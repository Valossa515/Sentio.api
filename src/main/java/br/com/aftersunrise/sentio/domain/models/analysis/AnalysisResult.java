package br.com.aftersunrise.sentio.domain.models.analysis;

import br.com.aftersunrise.sentio.domain.models.DatabaseEntityBase;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.Sentiment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AnalysisResult extends DatabaseEntityBase implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Lob
    @Column(nullable = false, name = "original_text")
    private String originalText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sentiment sentiment;

    @Column(nullable = false, name = "confidence_score")
    private Double confidenceScore;

    @Column(nullable = false, updatable = false, name = "analysis_timestamp")
    private LocalDateTime analysisTimestamp;

    @PrePersist
    public void onPrePersist() {
        analysisTimestamp = LocalDateTime.now();
    }
}