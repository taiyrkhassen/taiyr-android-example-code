class ExampleRepositoryImpl(
    private val networkApi: NetworkApi
) : ExampleRepository {

    override suspend fun getProductLimits(id: String): List<Limit> {
        val listLimits = networkApi.getProductLimits(id)
        val mainList = listsLimits.mainListDto?.map { it.toDomain(Limit.GroupType.Main) } ?: emptyList()
        val otherList = listsLimits.otherListDto?.map { it.toDomain(Limit.GroupType.Other) } ?: emptyList()
        return mainList + otherList
    }
}