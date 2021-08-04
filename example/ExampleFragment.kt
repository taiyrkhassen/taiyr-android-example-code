class ExampleFragment : BaseFragment<ExampleViewModel>(R.layout.fragment_opening_new_card),
    ProductsApi.SomeListener {

    companion object {
        private const val ARG_SOME_PRODUCT = "arg_some_product"
        
        fun newInstance() = ExampleFragment()

        fun getScreen(someProduct: CardProduct, someProducts: ArrayList<CardProduct>) = BaseScreen(
            newInstance().apply {
                arguments = bundleOf(
                    ARG_SOME_PRODUCT to someProduct
                )
            })
    }

    override val viewModel: ExampleViewModel by lazy {
        ProductsFeatureHolder.getKoin(requireContext()).inject(
            this,
            parametersOf(
                ProductsApi.getCardProducts(requireArguments()),
                ProductsApi.getCardProduct(requireArguments())
            )
        )
    }

    private val productAdapter by lazy {
        VirtualCardsAdapter()
    }

    private val infoAdapter = CompositeAdapter(InfoDelegateAdapter())

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            viewModel.onCardProductSelected(position)
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    override fun prepareUI() {
        super.prepareUI()

        btnActionBack.setOnClickListener {
            requireRouter().exit()
        }

        nsvOpeningNewCard.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            appBar.isSelected = nsvOpeningNewCard.canScrollVertically(-1)
            cvCardOrder.isSelected = nsvOpeningNewCard.canScrollVertically(1)
        }

        Product.adapter = productAdapter
        ProductTabbar.addOnPageChangeListener(onPageChangeListener)
        diPlasticCards.setViewPager(ProductTabbar)

        rvInfo.adapter = infoAdapter

        btnAccountOrder.setOnClickListener {
            viewModel.onCardOrderClicked()
        }

        tietSelectCurrency.setOnClickListener {
            viewModel.onSelectCurrencyClicked()
        }
    }

    override fun observeViewModel() {
        super.observeViewModel()

        viewModel.someProducts.observe(viewLifecycleOwner, {
            productAdapter.update(it)
            selectProduct()
        })

        viewModel.selectedProduct.observe(viewLifecycleOwner, {
            selectProduct(it)
        })

        viewModel.navigateToSelectCurrencyLiveEvent.observe(viewLifecycleOwner, { currencies ->
            currencies?.let {
                val selectCurrencyScreen = SelectCurrencyBottomDialogFragment.getScreen(it)
                requireRouter().navigateTo(selectCurrencyScreen)
            }
        })

        viewModel.details.observe(viewLifecycleOwner, {
            infoAdapter.update(it)
        })

        viewModel.selectedCurrency.observe(viewLifecycleOwner, { selectedCurrency ->
            tietSelectCurrency.setText(
                selectedCurrency?.currency?.getShortNameWithUISymbol(
                    requireContext()
                ) ?: ""
            )
        })

        viewModel.navigateToCardProductConfirmationLiveEvent.observe(viewLifecycleOwner, {
            val selectedProduct = viewModel.selectedProduct.value ?: return@observe
            val selectedCurrency = viewModel.selectedCurrency.value?.currency ?: return@observe
            val info: ArrayList<Any> = arrayListOf(
                CardProductInfo.CardName(selectedProduct.title),
                CardProductInfo.CardType(selectedProduct.type),
                CardProductInfo.Currency(selectedCurrency)
            )

            if (selectedProduct.type == CardProduct.Type.Virtual) {
                val screen = CardProductConfirmationFragment.getScreen(
                    someProduct = selectedProduct,
                    info = info,
                    currency = selectedCurrency
                )
                requireRouter().navigateTo(screen)
            }
        })

        viewModel.navigateToProductSetInfoLiveEvent.observe(viewLifecycleOwner, {
            val setInfoScreen =
                requireContext().getFeature(ProductsApi::class).getCardProductSetInfoScreen(it)
            requireRouter().navigateTo(setInfoScreen)
        })

        viewModel.navigateToUnderDev.observe(viewLifecycleOwner, {
            requireRouter().navigateTo(UnderDevDialogFragment().asScreen())
        })
    }

    private fun selectProduct(someProduct: CardProduct? = null) {
        val someProducts = viewModel.someProducts.value ?: listOf()
        val selectedProduct = someProduct ?: viewModel.selectedProduct.value
        val selectedProductIndex =
            someProducts.indexOfFirst { it.title == selectedProduct?.title }
        if (selectedProductIndex != -1) {
            ProductTabbar.currentItem = selectedProductIndex
        }

        AccountNameLabel.text = selectedProduct?.title

        tvStatus.text = when (someProduct?.type) {
            CardProduct.Type.Virtual -> someProduct.getStatus(requireContext())
            else -> someProduct?.getTitleAndStatus(requireContext())
        }

        someProduct?.currencies?.let {
            AccountCurrencyButton.visibility = when {
                it.size > 1 -> View.VISIBLE
                else -> View.GONE
            }
        }

        someProduct?.detailsList?.let {
            tvDescriptionNewCard.visibility = when {
                it.isNullOrEmpty() -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    override fun onCurrencySelected(currency: SelectableCurrency) {
        viewModel.onCurrencySelected(currency)
    }
}
