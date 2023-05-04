package by.kaminsky.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@ToString
@Builder
@Entity
@Table(name = "materials", indexes = @Index(columnList = "material_name", name = "materials_name_idx"),
        uniqueConstraints = {@UniqueConstraint(columnNames = {"material_name", "specific"})})
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "material_name", nullable = false)
    private String name;
    @Column(name = "specific", nullable = false)
    private String specific;
    @Column(name = "packaging", nullable = false)
    private String packaging;
    @Column(name = "cost", nullable = false)
    private BigDecimal cost;
    @Column(name = "lastupd", nullable = false)
    private LocalDateTime lastUpdate;

}


