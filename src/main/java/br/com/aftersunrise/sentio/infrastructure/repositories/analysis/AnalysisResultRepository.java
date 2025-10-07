package br.com.aftersunrise.sentio.infrastructure.repositories.analysis;

import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {
}