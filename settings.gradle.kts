rootProject.name = "Tagify"

// Le module Premium (fermé, non publié sur GitHub) n'est inclus dans le build
// que s'il est présent localement. Un clone public ne compile donc que le Core.
if (file("tagify-premium/build.gradle.kts").exists()) {
    include("tagify-premium")
}
