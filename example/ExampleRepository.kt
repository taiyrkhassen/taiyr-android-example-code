interface ExampleRepository {
    suspend fun getProductLimits(id: String): List<Limit>
}