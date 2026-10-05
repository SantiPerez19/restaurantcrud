plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // PostgreSQL Driver
    runtimeOnly("org.postgresql:postgresql:42.5.0")

    // iText para generar PDFs
    implementation("com.itextpdf:itextpdf:5.5.13.3")

    // Apache PDFBox
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
}

tasks.test {
    useJUnitPlatform()
}