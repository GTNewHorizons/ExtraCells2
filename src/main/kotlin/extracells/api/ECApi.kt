package extracells.api

object ECApi {
    fun instance(): ExtraCellsApi? {
        if (instance == null) {
            try {
                instance = Class
                        .forName("extracells.ExtraCellsApiInstance")
                        .getField("instance")[null] as ExtraCellsApi
            } catch (e: Exception) {
            }
        }
        return instance
    }

    private var instance: ExtraCellsApi? = null
}