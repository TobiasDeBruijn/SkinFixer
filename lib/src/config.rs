//! Module containing all things related to config

use std::path::{Path, PathBuf};
use std::sync::{Arc, Mutex};
use std::cell::RefCell;
use std::str::FromStr;

lazy_static::lazy_static! {
    /// Configuration instance
    /// This is static because of JNI we do not have a nice clean way of keeping an instance around without going static
    pub static ref CONFIG: Arc<Mutex<RefCell<Option<Config>>>> = Arc::new(Mutex::new(RefCell::new(None)));
}

/// Unwrap an Option<String> to a String, but panic! if the Option is None
macro_rules! unwrap_str {
    ($expression:expr, $name:literal) => {
        match $expression {
            Some(e) => e,
            None => panic!("Config variable '{}' is not allowed to be None.", $name)
        }
    }
}

/// Struct describing the configuration
pub struct Config {
    /// The type of storage being being used
    storage_type:   StorageType,
    /// SQL: host
    host:           Option<String>,
    /// SQL: database
    database:       Option<String>,
    /// SQL: username
    username:       Option<String>,
    /// SQL: password
    password:       Option<String>,
    /// Path to plugin folder
    storage_path:   Option<String>,
    /// MySQL connection pool
    pool:           Option<mysql::Pool>
}

impl Config {
    /// Get the Path to the Plugin's data folder
    ///
    /// # Errors
    /// Err when self.storage_path is None
    pub fn get_path(&self) -> Result<&Path, String> {
        match &self.storage_path {
            Some(p) => Ok(Path::new(p)),
            None=> Err("File storage/SQLite is not used as storage backend.".to_string())
        }
    }

    /// Get the type of storage backend being used
    pub fn get_type(&self) -> &StorageType {
        &self.storage_type
    }

    /// Get a MySQL database connection
    ///
    /// # Errors
    /// Err when getting a connection from the pool failed, or if MySQL is not used as storage backend
    pub fn mysql_conn(&self) -> Result<mysql::PooledConn, String> {
        let pool = match &self.pool {
            Some(p) => p,
            None => return Err("MySQL is not being used as storage backend, thus no Pool is available.".to_string())
        };

        match pool.get_conn() {
            Ok(c) => Ok(c),
            Err(e) => Err(format!("Failed to create PooledConn: {:?}", e))
        }
    }

    /// Get a PostgreSQL database connection
    ///
    /// # Errors
    /// Err when connecting to the database failed, or when PostgreSQL is not used as database backend
    pub fn postgres_conn(&self) -> Result<postgres::Client, String> {
        let uri = format!("postgresql://{}:{}@{}/{}", unwrap_str!(&self.username, "sqlSettings.username"), unwrap_str!(&self.password, "sqlSettings.password"), unwrap_str!(&self.host, "sqlSettings.host"), unwrap_str!(&self.database, "sqlSettings.database"));
        match postgres::Client::connect(&uri, postgres::NoTls) {
            Ok(c) => Ok(c),
            Err(e) => Err(format!("Failed to create PostgreSQL connection: {:?}", e))
        }
    }

    /// Get a SQLite database connection
    ///
    /// # Errors
    /// Err if SQLite is not being used as storage backend, or opening the connection failed
    pub fn sqlite_conn(&self) -> Result<rusqlite::Connection, String> {
        let mut path = PathBuf::from(match &self.storage_path {
            Some(p) => p,
            None => return Err("SQLite is not being used as storage backend.".to_string())
        });

        path.push("skins.db3");
        match rusqlite::Connection::open(&path) {
            Ok(c) => Ok(c),
            Err(e) => Err(e.to_string())
        }
    }

    /// Create a new instance of Config
    pub fn new(storage_type: StorageType, host: Option<String>, database: Option<String>, username: Option<String>, password: Option<String>, storage_path: Option<String>) -> Self {
        let pool = match storage_type {
            StorageType::Mysql => {
                let opts = mysql::OptsBuilder::new()
                    .db_name(Some(unwrap_str!(&database, "sqlSettings.database")))
                    .ip_or_hostname(Some(unwrap_str!(&host, "sqlSettings.host")))
                    .user(Some(unwrap_str!(&username, "sqlSettings.username")))
                    .pass(Some(unwrap_str!(&password, "sqlSettings.password")));

                let pool = match mysql::Pool::new(opts) {
                    Ok(p) => p,
                    Err(e) => panic!("Failed to create MySQL Pool: {:?}", e)
                };

                Some(pool)
            },
            _ => None
        };

        Self {
            storage_type,
            host,
            database,
            username,
            password,
            storage_path,
            pool
        }
    }
}

/// The type of storage backend being used
pub enum StorageType {
    /// MySQL Database
    Mysql,
    /// PostgreSQL database
    Postgres,
    /// Binary format
    Bin,
    /// SQLite database
    Sqlite
}

impl FromStr for StorageType {
    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "mysql" => Ok(StorageType::Mysql),
            "postgres" => Ok(StorageType::Postgres),
            "bin" => Ok(StorageType::Bin),
            "sqlite" => Ok(StorageType::Sqlite),
            _ => Err(())
        }
    }
}