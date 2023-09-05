# akka-migration-example

Presented during [WSUG meetup](https://www.meetup.com/wroclaw-scala-user-group/events/295606178)

This repository contains exemplary HTTP server and client applications, showcasing a migration between Akka HTTP and http4s. You can run both server and client using scala-cli. Client performs a single request to the server and logs the result.

To run the server:

```bash
scala-cli run . --main-class pkowalcze.akkamigration.server.ServerApp
```

To run the client: 

```bash
scala-cli run . --main-class pkowalcze.akkamigration.client.ClientApp
```

Akka HTTP version is available on tag `akka-http` and http4s on tag `http4s`.