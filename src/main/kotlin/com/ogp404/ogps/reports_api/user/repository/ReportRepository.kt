package com.ogp404.ogps.reports_api.report.repository

import com.ogp404.ogps.reports_api.report.domain.Report
import com.ogp404.ogps.reports_api.report.domain.ReportId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportRepository : JpaRepository<Report, ReportId>