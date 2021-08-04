class OpeningNewCardViewModel(
    private val products: MutableLiveData<List<CardProduct>>,
    private val _selectedProduct: MutableLiveData<CardProduct>,
    private val exampleRepository: ExampleRepository
) : BaseViewModel() {

    val products = products.toLiveData()

    private val _currencies = MutableLiveData<List<SelectableCurrency?>?>()

    private val _selectedCurrency = MutableLiveData<SelectableCurrency>()
    val selectedCurrency = _selectedCurrency.toLiveData()
    val selectedProduct = _selectedProduct.toLiveData()

    private val _details = MutableLiveData<List<Any>>()
    val details = _details.toLiveData()

    val navigateToSelectCurrencyLiveEvent = LiveEvent<List<SelectableCurrency>>()
    val navigateToCardProductConfirmationLiveEvent = LiveEvent<Unit>()
    val navigateToCardProductSetInfoLiveEvent = LiveEvent<Pair<CardProduct, Currency>>()

    init {
        onRefresh()
    }

    private fun onRefresh() {
        _details.value = _selectedProduct.value?.detailsList
        _currencies.value = _selectedProduct.value?.currencies?.map {
            SelectableCurrency().apply {
                this.currency = it
            }
        }

        if (!_currencies.value.isNullOrEmpty()) {
            val selectedCurrency = _currencies.value?.get(0) ?: return
            onCurrencySelected(selectedCurrency)
        }
    }

    fun onProductSelected(position: Int) {
        val product = products.value?.getOrNull(position)
        if (_selectedProduct.value?.title != product?.title) {
            _selectedProduct.value = product
            onRefresh()
        }
    }

    fun onSelectCurrencyClicked() {
        navigateToSelectCurrencyLiveEvent.value = _currencies.value?.filterNotNull() ?: return
    }

    fun onCurrencySelected(selectableCurrency: SelectableCurrency) {
        _selectedCurrency.value = selectableCurrency
        _currencies.value?.let { currencies ->
            currencies.forEach { currency ->
                currency?.isChecked = currency?.currency == selectableCurrency.currency
            }
        }
    }

    fun getProductLimits() {
        _isProgressVisible.postValue(true)
        val getProductLimits = CoroutineTask(
            id = "getProductLimitsTask",
            strategy = Task.Strategy.KillFirst,
            function = { exampleRepository.getProductLimits(selectedProduct.id) },
            onSuccess = { listOfLimits ->
                _isProgressVisible.postValue(false)
                _limits.postValue(listOfLimits)
            },
            onError = {
                _isProgressVisible.postValue(false)
                _exception.value = it
            },
            scope = viewModelScope
        )
        taskHandler handle getProductLimits
    }
}