use std::fs;
use std::path::Path;
use actix_web::{App, HttpServer};
use once_cell::sync::OnceCell;

mod service;
mod component_type;

static AUTH_KEY: OnceCell<String> = OnceCell::new();

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    let path = Path::new("key.auth");
    if !path.is_file() {
        return Err(std::io::Error::new(std::io::ErrorKind::NotFound, "Auth key not found!"));
    }
    let key = fs::read_to_string(path).expect("Unable to read auth key!");
    AUTH_KEY.set(key).expect("Unable to set auth key!");

    HttpServer::new(|| {
        App::new()
            .service(service::new)
            .service(service::get)
            .service(service::get_file)
            .service(service::post_file)
            .service(service::complete)
            .service(service::hash)
    })
    .bind("0.0.0.0:9090")?
    .run()
    .await
}

pub fn get_auth_key() -> String {
    return AUTH_KEY.get().expect("Auth key not set!").clone();
}
