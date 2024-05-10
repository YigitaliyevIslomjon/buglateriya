package uz.org

import jakarta.persistence.*
import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @CreatedBy var createdBy: String? = null,
    @LastModifiedBy var modifiedBy: String? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false,
)

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["pinfl", "organization"])])
class Employee(
    @Column(nullable = false, length = 50) var firstName: String,
    @Column(nullable = false, length = 50) var lastName: String,
    @Column(length = 50, nullable = false) var pinfl: Long,
    @Column(nullable = false) var hireDate: LocalDate,
    @ManyToOne var organization: Organization
) : BaseEntity()

@Entity
class Organization(
    @Column(nullable = false) var name: String,
    @ManyToOne var region: Region,
    @ManyToOne var parent: Organization? = null
) : BaseEntity()

@Entity
class Region(
    @Column(nullable = false, length = 100, unique = true) var name: String,
) : BaseEntity()

@Entity
class CalculationTable(
    @ManyToOne var employee: Employee,
    @Column(nullable = false) var amount: BigDecimal,
    @Column(nullable = false) var rate: Double,
    @Column(nullable = false) var date: LocalDate,
    @ManyToOne var organization: Organization,
    @Enumerated(EnumType.STRING) var calculationType: CalculationType
) : BaseEntity()
