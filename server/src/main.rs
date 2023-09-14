use actix_web::{App, HttpServer};
mod service;
mod component_type;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    HttpServer::new(|| {
        App::new()
            .service(service::get_file)
            .service(service::complete)
    })
    .bind("0.0.0.0:9090")?
    .run()
    .await
}
