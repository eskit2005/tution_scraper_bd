CREATE TABLE User (
    id INT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE Admin (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE Donor (
    id INT PRIMARY KEY,
    FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE
);

DROP TABLE IF EXISTS Beneficiary;

CREATE TABLE Beneficiary (
                             id INT PRIMARY KEY,
                             reason TEXT,
                             dependents INT,
                             income DECIMAL(12, 2),
                             emergency BOOLEAN DEFAULT FALSE,
                             disability BOOLEAN DEFAULT FALSE,
                             age INT,
                             is_orphan BOOLEAN DEFAULT FALSE,
                             has_debt BOOLEAN DEFAULT FALSE,
                             unemployed BOOLEAN DEFAULT FALSE,
                             illness BOOLEAN DEFAULT FALSE,
                             priority_score INT,
                             eligible BOOLEAN DEFAULT FALSE,
                             reject_reason TEXT,
                             checked_at DATETIME,
                             total_received_value DECIMAL(12, 2) DEFAULT 0,
                             registered DATETIME,
                             FOREIGN KEY (id) REFERENCES User(id) ON DELETE CASCADE
);

CREATE TABLE Receipt (
    id INT AUTO_INCREMENT,
    recep_num VARCHAR(100) UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    issued_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    d_id INT NOT NULL,
    PRIMARY KEY (id, d_id),
    FOREIGN KEY (d_id) REFERENCES Donor(id) ON DELETE CASCADE
);

CREATE TABLE Inventory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    appox_value DECIMAL(10, 2),
    status VARCHAR(50),
    received_at DATETIME,
    d_id INT,
    FOREIGN KEY (d_id) REFERENCES Donor(id) ON DELETE SET NULL
);

CREATE TABLE Zakat_Assignment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount_assigned DECIMAL(10, 2),
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    inven_id INT,
    FOREIGN KEY (inven_id) REFERENCES Inventory(id) ON DELETE SET NULL
);

CREATE TABLE Beneficiary_donor (
    D_id INT,
    B_id INT,
    amount DECIMAL(10, 2) NOT NULL,
    donated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (D_id, B_id),
    FOREIGN KEY (D_id) REFERENCES Donor(id) ON DELETE CASCADE,
    FOREIGN KEY (B_id) REFERENCES Beneficiary(id) ON DELETE CASCADE
);

CREATE TABLE Beneficiary_admin_zakat (
    z_id INT,
    b_id INT,
    a_id INT,
    PRIMARY KEY (z_id, b_id, a_id),
    FOREIGN KEY (z_id) REFERENCES Zakat_Assignment(id) ON DELETE CASCADE,
    FOREIGN KEY (b_id) REFERENCES Beneficiary(id) ON DELETE CASCADE,
    FOREIGN KEY (a_id) REFERENCES Admin(id) ON DELETE CASCADE
);

