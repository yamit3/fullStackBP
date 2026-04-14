# Test Data Summary

## Overview
Generated comprehensive test data for the FullStack BP application with realistic samples for all three main entities.

---

## 1. CLIENTS (3 rows)

| ID | Name | Gender | Age | Identification | Address | Phone | Active |
|---|---|---|---|---|---|---|---|
| 1 | Juan Carlos Pérez | MALE | 35 | 1234567890 | Av. Principal 123, Quito | 0987654321 | true |
| 2 | María Elena García López | FEMALE | 28 | 1234567891 | Calle Secundaria 456, Guayaquil | 0987654322 | true |
| 3 | Roberto Andrés Mendoza Ruiz | MALE | 42 | 1234567892 | Avenida Central 789, Cuenca | 0987654323 | true |

**Password Format:** `{salt}:{hash}` (SHA-256 with random salt)

---

## 2. ACCOUNTS (6 rows - 2 per client)

| ID | Number | Type | Initial Balance | Client | Active |
|---|---|---|---|---|---|
| 1 | 100001 | CHECKING | $5,000.00 | Juan Carlos Pérez | true |
| 2 | 100002 | SAVINGS | $10,000.00 | Juan Carlos Pérez | true |
| 3 | 100003 | CHECKING | $3,500.50 | María Elena García López | true |
| 4 | 100004 | SAVINGS | $25,000.00 | María Elena García López | true |
| 5 | 100005 | CHECKING | $7,200.75 | Roberto Andrés Mendoza Ruiz | true |
| 6 | 100006 | SAVINGS | $50,000.00 | Roberto Andrés Mendoza Ruiz | true |

**Note:** Account numbers are generated in service as incremental values (`MAX(number) + 1`).

---

## 3. MOVEMENTS (18 rows - 3 per account)

### Account 1 (Juan's Checking)
| ID | Date | Type | Amount | Balance | Active |
|---|---|---|---|---|---|
| 1 | 2024-04-01 10:30:00 | DEPOSIT | $5,000.00 | $5,000.00 | true |
| 2 | 2024-04-05 14:15:00 | WITHDRAW | -$500.00 | $4,500.00 | true |
| 3 | 2024-04-10 09:45:00 | DEPOSIT | $1,000.00 | $5,500.00 | true |

### Account 2 (Juan's Savings)
| ID | Date | Type | Amount | Balance | Active |
|---|---|---|---|---|---|
| 4 | 2024-04-02 11:20:00 | DEPOSIT | $10,000.00 | $10,000.00 | true |
| 5 | 2024-04-08 15:30:00 | DEPOSIT | $2,000.00 | $12,000.00 | true |
| 6 | 2024-04-12 16:45:00 | WITHDRAW | -$1,500.00 | $10,500.00 | true |

### Account 3 (María's Checking)
| ID | Date | Type | Amount | Balance | Active |
|---|---|---|---|---|---|
| 7 | 2024-03-28 08:00:00 | DEPOSIT | $3,500.50 | $3,500.50 | true |
| 8 | 2024-04-03 13:20:00 | WITHDRAW | -$250.00 | $3,250.50 | true |
| 9 | 2024-04-11 10:15:00 | DEPOSIT | $750.00 | $4,000.50 | true |

### Account 4 (María's Savings)
| ID | Date | Type | Amount | Balance | Active |
|---|---|---|---|---|---|
| 10 | 2024-03-25 09:30:00 | DEPOSIT | $25,000.00 | $25,000.00 | true |
| 11 | 2024-04-06 14:45:00 | DEPOSIT | $5,000.00 | $30,000.00 | true |
| 12 | 2024-04-09 11:00:00 | WITHDRAW | -$3,000.00 | $27,000.00 | true |

### Account 5 (Roberto's Checking)
| ID | Date | Type | Amount | Balance | Active |
|---|---|---|---|---|---|
| 13 | 2024-03-30 10:00:00 | DEPOSIT | $7,200.75 | $7,200.75 | true |
| 14 | 2024-04-04 12:30:00 | WITHDRAW | -$800.00 | $6,400.75 | true |
| 15 | 2024-04-13 09:15:00 | DEPOSIT | $1,200.00 | $7,600.75 | true |

### Account 6 (Roberto's Savings)
| ID | Date | Type | Amount | Balance | Active |
|---|---|---|---|---|---|
| 16 | 2024-03-20 07:45:00 | DEPOSIT | $50,000.00 | $50,000.00 | true |
| 17 | 2024-04-07 15:20:00 | DEPOSIT | $10,000.00 | $60,000.00 | true |
| 18 | 2024-04-11 14:30:00 | WITHDRAW | -$5,000.00 | $55,000.00 | true |

---

## Data Relationships

```
Client 1 (Juan)
├── Account 1 (CHECKING)
│   ├── Movement 1 (DEPOSIT)
│   ├── Movement 2 (WITHDRAW)
│   └── Movement 3 (DEPOSIT)
└── Account 2 (SAVINGS)
    ├── Movement 4 (DEPOSIT)
    ├── Movement 5 (DEPOSIT)
    └── Movement 6 (WITHDRAW)

Client 2 (María)
├── Account 3 (CHECKING)
│   ├── Movement 7 (DEPOSIT)
│   ├── Movement 8 (WITHDRAW)
│   └── Movement 9 (DEPOSIT)
└── Account 4 (SAVINGS)
    ├── Movement 10 (DEPOSIT)
    ├── Movement 11 (DEPOSIT)
    └── Movement 12 (WITHDRAW)

Client 3 (Roberto)
├── Account 5 (CHECKING)
│   ├── Movement 13 (DEPOSIT)
│   ├── Movement 14 (WITHDRAW)
│   └── Movement 15 (DEPOSIT)
└── Account 6 (SAVINGS)
    ├── Movement 16 (DEPOSIT)
    ├── Movement 17 (DEPOSIT)
    └── Movement 18 (WITHDRAW)
```

---

## Sequence Resets

After all inserts, sequences are reset for next auto-generated values:

| Sequence | Resets To | Usage |
|---|---|---|
| `person_seq` | 4 | Next Person/Client ID |
| `account_seq` | 7 | Next Account ID |
| `movement_seq` | 19 | Next Movement ID |

---

## Features

✅ **3 Clients** - Diverse demographic data (male/female, different ages)  
✅ **6 Accounts** - 2 per client, mixed account types (CHECKING & SAVINGS)  
✅ **18 Movements** - 3 per account, realistic transaction history  
✅ **Soft Delete Ready** - All records have `active = true` for soft-delete testing  
✅ **Auto-Generated Numbers** - Account numbers follow service-side incremental generation (`MAX + 1`)  
✅ **Proper Relationships** - All foreign keys properly linked  
✅ **Date Range Variety** - Movements spread across March-April 2024  

---

## Build Status

✅ `EXIT_CODE=0` - Tests pass with test data loaded  
✅ All sequences reset correctly  
✅ No constraint violations  
