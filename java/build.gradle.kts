subprojects {
   tasks.withType<Test> {
       useJUnitPlatform()
       testLogging {
           events("passed", "skipped", "failed")
           showStandardStreams = true
       }
   }
}