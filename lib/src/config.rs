use std::str::FromStr;
use std::cell::RefCell;
use std::sync::{Arc, Mutex};
use std::path::Path;

lazy_static::lazy_static! {
    pub static ref CONFIG: Arc<Mutex<RefCell<Option<Config>>>> = Arc::new(Mutex::new(RefCell::new(None)));
}

macro_rules! unwrap_str {
    ($expression:expr, $name:literal) => {
        match $expression {
            Some(e) => e,
            None => panic!("Config variable '{}' is not allowed to be None.", $name)
        }
    }
}

pub struct Config {
    storage_type:   StorageType,
    host:           Option<String>,
    database:       Option<String>,
    username:       Option<String>,
    password:       Option<String>,
    storage_path:   Option<String>,
    pool:           Option<mysql::Pool>
}

impl Config {
    pub fn get_path(&self) -> Result<&Path, String> {
        match &self.storage_path {
            Some(p) => Ok(Path::new(p)),
            None=> Err("File storage is not used as storage backend.".to_string())
        }
    }

    pub fn get_type(&self) -> &StorageType {
        &self.storage_type
    }

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

    pub fn postgres_conn(&self) -> Result<postgres::Client, String> {
        let uri = format!("postgresql://{}:{}@{}/{}", unwrap_str!(&self.username, "sqlSettings.username"), unwrap_str!(&self.password, "sqlSettings.password"), unwrap_str!(&self.host, "sqlSettings.host"), unwrap_str!(&self.database, "sqlSettings.database"));
        match postgres::Client::connect(&uri, postgres::NoTls) {
            Ok(c) => Ok(c),
            Err(e) => Err(format!("Failed to create PostgreSQL connection: {:?}", e))
        }
    }

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

pub enum StorageType {
    Mysql,
    Postgres,
    Bin
}

impl FromStr for StorageType {
    type Err = ();

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s.to_lowercase().as_str() {
            "mysql" => Ok(StorageType::Mysql),
            "postgres" => Ok(StorageType::Postgres),
            "bin" => Ok(StorageType::Bin),
            _ => Err(())
        }
    }
}