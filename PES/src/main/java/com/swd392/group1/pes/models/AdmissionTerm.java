package com.swd392.group1.pes.models;

import com.swd392.group1.pes.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "`admission_term`")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdmissionTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String name;

    @Column(name = "`start_date`")
    LocalDateTime startDate;

    @Column(name = "`end_date`")
    LocalDateTime endDate;

    Integer year;

    @Enumerated(EnumType.STRING)
    Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_term_id")
    AdmissionTerm parentTerm;

    @OneToMany(mappedBy = "admissionTerm", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    List<TermItem> termItemList;
}
