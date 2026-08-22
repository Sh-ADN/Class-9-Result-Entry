package com.abutorab.resultentry.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NetworkManager {
    private val scriptUrl = "https://script.google.com/macros/s/AKfycbwVR0HtbBCosJVx0HomgVScJVoRoZDP-B8nPsLWjd1jRM3LL20LYxTzfuc7hxJgTmtz5A/exec"

    private fun JSONObject.optIntOrNull(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        val raw = opt(key)
        if (raw is Number) return raw.toInt()
        if (raw is String) return raw.trim().toDoubleOrNull()?.toInt() ?: raw.trim().toIntOrNull()
        return null
    }

    private fun JSONObject.optDoubleOrNull(key: String): Double? {
        if (!has(key) || isNull(key)) return null
        val raw = opt(key)
        if (raw is Number) return raw.toDouble()
        if (raw is String) return raw.trim().toDoubleOrNull()
        return null
    }

    private fun JSONObject.optStringSafe(key: String): String {
        if (!has(key) || isNull(key)) return ""
        return optString(key, "")
    }

    suspend fun getRoster(section: String): List<Roster> = withContext(Dispatchers.IO) {
        val url = URL("$scriptUrl?action=getRoster&section=$section")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText().trim()
            if (response.startsWith("{")) {
                val jsonObj = JSONObject(response)
                if (jsonObj.has("error")) {
                    throw Exception(jsonObj.getString("error"))
                }
            }
            val jsonArray = JSONArray(response)
            val rosterList = mutableListOf<Roster>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val roll = item.optIntOrNull("roll") ?: (i + 1)
                val name = item.optStringSafe("name")
                rosterList.add(
                    Roster(
                        roll = roll,
                        name = name,
                        section = section
                    )
                )
            }
            rosterList
        } else {
            throw Exception("Failed to fetch roster: ${connection.responseCode}")
        }
    }

    suspend fun getEntries(section: String): List<Entry> = withContext(Dispatchers.IO) {
        val url = URL("$scriptUrl?action=getEntries&section=$section")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText().trim()
            if (response.startsWith("{")) {
                val jsonObj = JSONObject(response)
                if (jsonObj.has("error")) {
                    throw Exception(jsonObj.getString("error"))
                }
            }
            val jsonArray = JSONArray(response)
            val entryList = mutableListOf<Entry>()
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val roll = item.optIntOrNull("roll") ?: continue
                val totalMarks = item.optIntOrNull("totalMarks") ?: item.optIntOrNull("marks")
                val failedCount = item.optIntOrNull("failedCount") ?: item.optIntOrNull("failed")
                val gpa = item.optDoubleOrNull("gpa")
                
                if (totalMarks != null || failedCount != null || gpa != null) {
                    entryList.add(
                        Entry(
                            roll = roll,
                            section = section,
                            totalMarks = totalMarks ?: 0,
                            failedCount = failedCount ?: 0,
                            gpa = gpa,
                            syncState = SyncState.SYNCED,
                            localUpdatedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
            entryList
        } else {
            throw Exception("Failed to fetch entries: ${connection.responseCode}")
        }
    }

    suspend fun saveEntry(entry: Entry): Boolean = withContext(Dispatchers.IO) {
        val url = URL(scriptUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "text/plain")
        connection.doOutput = true
        
        val json = JSONObject().apply {
            put("action", "saveEntry")
            put("section", entry.section)
            put("roll", entry.roll)
            put("totalMarks", entry.totalMarks)
            put("failedCount", entry.failedCount)
            if (entry.gpa != null) put("gpa", entry.gpa)
        }
        
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(json.toString())
        writer.flush()
        writer.close()
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            val respJson = JSONObject(response)
            respJson.optBoolean("success", false) || respJson.has("success")
        } else {
            false
        }
    }

    suspend fun getSummary(): SummaryResponse = withContext(Dispatchers.IO) {
        val url = URL("$scriptUrl?action=getSummary")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText().trim()
            val jsonObj = JSONObject(response)
            if (jsonObj.has("error")) {
                throw Exception(jsonObj.getString("error"))
            }
            val studentsArr = jsonObj.getJSONArray("students")
            val students = mutableListOf<SummaryStudent>()
            for (i in 0 until studentsArr.length()) {
                val item = studentsArr.getJSONObject(i)
                students.add(
                    SummaryStudent(
                        oldRoll = item.getInt("oldRoll"),
                        section = item.getString("section"),
                        name = item.getString("name"),
                        totalMarks = item.getInt("totalMarks"),
                        failedCount = item.getInt("failedCount"),
                        gpa = if (item.has("gpa") && !item.isNull("gpa")) item.getDouble("gpa") else null,
                        category = item.getString("category"),
                        newRoll = if (item.has("newRoll") && !item.isNull("newRoll")) item.getInt("newRoll") else null
                    )
                )
            }
            val countsObj = jsonObj.getJSONObject("counts")
            val counts = SummaryCounts(
                meritCount = countsObj.getInt("meritCount"),
                tier1Count = countsObj.getInt("tier1Count"),
                tier2Count = countsObj.getInt("tier2Count"),
                tier3Count = countsObj.getInt("tier3Count"),
                notPassedCount = countsObj.getInt("notPassedCount"),
                passCount = countsObj.getInt("passCount"),
                totalStudents = countsObj.getInt("totalStudents")
            )
            SummaryResponse(students, counts)
        } else {
            throw Exception("Failed to fetch summary: ${connection.responseCode}")
        }
    }

    suspend fun compileResults(): SummaryCounts = withContext(Dispatchers.IO) {
        val url = URL(scriptUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "text/plain")
        connection.doOutput = true
        
        val json = JSONObject().apply { put("action", "compileResults") }
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(json.toString())
        writer.flush()
        writer.close()
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            val jsonObj = JSONObject(response)
            if (jsonObj.has("error")) {
                throw Exception(jsonObj.getString("error"))
            }
            SummaryCounts(
                meritCount = jsonObj.getInt("meritCount"),
                tier1Count = jsonObj.getInt("tier1Count"),
                tier2Count = jsonObj.getInt("tier2Count"),
                tier3Count = jsonObj.getInt("tier3Count"),
                notPassedCount = jsonObj.getInt("notPassedCount"),
                passCount = jsonObj.getInt("passCount"),
                totalStudents = jsonObj.getInt("totalStudents")
            )
        } else {
            throw Exception("Failed to compile results: ${connection.responseCode}")
        }
    }

    suspend fun generateDoc(): GenerateDocResponse = withContext(Dispatchers.IO) {
        val url = URL(scriptUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "text/plain")
        connection.doOutput = true
        
        val json = JSONObject().apply { put("action", "generateDoc") }
        val writer = OutputStreamWriter(connection.outputStream)
        writer.write(json.toString())
        writer.flush()
        writer.close()
        
        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = reader.readText()
            val jsonObj = JSONObject(response)
            if (jsonObj.has("error")) {
                throw Exception(jsonObj.getString("error"))
            }
            GenerateDocResponse(
                docUrl = jsonObj.getString("docUrl")
            )
        } else {
            throw Exception("Failed to generate doc: ${connection.responseCode}")
        }
    }
}
