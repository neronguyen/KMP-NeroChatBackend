workspace "Chat System" {

    !identifiers hierarchical

    model {
        user = person "User" "A registered user who sends and receives messages."

        mailgun = softwareSystem "Mailgun" "Sends verification and notification emails." "Existing System"

        chatSystem = softwareSystem "Chat System" "Allows users to register and message each other." {
            desktopApp = container "Desktop App" "Lets users chat from a desktop computer." "Kotlin, Compose Multiplatform"
            androidApp = container "Android App" "Lets users chat from an Android device." "Kotlin, Compose Multiplatform"
            backend = container "Backend" "Handles authentication and messaging via a JSON/HTTPS API." "Kotlin, Spring Boot"
            database = container "Database" "Stores users, conversations, and messages." "PostgreSQL" "Database"
            cache = container "Cache" "Caches rate-limit counters and session data." "Redis" "Database"
        }

        user -> chatSystem.desktopApp "Uses" "GUI"
        user -> chatSystem.androidApp "Uses" "GUI"

        chatSystem.desktopApp -> chatSystem.backend "Makes API calls to" "JSON/HTTPS"
        chatSystem.androidApp -> chatSystem.backend "Makes API calls to" "JSON/HTTPS"

        chatSystem.backend -> chatSystem.database "Reads from and writes to" "JDBC"
        chatSystem.backend -> chatSystem.cache "Reads from and writes to" "RESP"

        chatSystem.backend -> mailgun "Sends e-mails to users using" "HTTPS API"
        mailgun -> user "Sends e-mails to" "SMTP"
    }

    views {
        systemContext chatSystem "SystemContext" {
            include *
            autoLayout lr
        }

        container chatSystem "Containers" {
            include *
            autoLayout lr
        }

        styles {
            element "Person" {
                shape person
                background #08427b
                color #ffffff
            }

            element "Software System" {
                background #1168bd
                color #ffffff
            }

            element "Existing System" {
                    background #999999
                    color #ffffff
            }

            element "Container" {
                background #438dd5
                color #ffffff
            }

            element "Database" {
                shape cylinder
            }
        }
    }

    configuration {
        scope softwaresystem
    }
}
