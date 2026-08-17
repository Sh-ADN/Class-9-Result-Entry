package com.abutorab.resultentry.data

data class SummaryStudent(
    val oldRoll: Int,
    val section: String,
    val name: String,
    val totalMarks: Int,
    val failedCount: Int,
    val gpa: Double?,
    val category: String,
    val newRoll: Int?
)

data class SummaryCounts(
    val meritCount: Int,
    val tier1Count: Int,
    val tier2Count: Int,
    val tier3Count: Int,
    val notPassedCount: Int,
    val passCount: Int,
    val totalStudents: Int
)

data class SummaryResponse(
    val students: List<SummaryStudent>,
    val counts: SummaryCounts
)

data class GenerateDocResponse(
    val docUrl: String,
    val pdfUrl: String
)
