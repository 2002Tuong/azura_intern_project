package com.slideshowmaker.slideshow.ui.premium

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.response.BillingPlan
import com.slideshowmaker.slideshow.databinding.ActivityPremiumPurchaseScreenBinding
import com.slideshowmaker.slideshow.ui.base.SnapSlideActivity
import com.slideshowmaker.slideshow.utils.extentions.launchAndRepeatOnLifecycleStarted
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.toDateFormat
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.*

class PremiumActivity : SnapSlideActivity(), KodeinAware {

    override val kodein by lazy { (application as VideoMakerApplication).kodein }

    val viewModel: PremiumPlanViewModel by instance()

    private lateinit var layoutBinding: ActivityPremiumPurchaseScreenBinding

    private lateinit var subscriptionAdapter: SubscriptionAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutBinding = ActivityPremiumPurchaseScreenBinding.inflate(layoutInflater)
        layoutBinding.backdropPager.adapter = PremiumBackDropAdapter()
        layoutBinding.btnSubscribe.setOnClickListener {
            subscriptionAdapter.productSelected?.let {
                viewModel.buyProduct(this, it)
            }
        }
        setContentView(layoutBinding.root)
        subscriptionAdapter = SubscriptionAdapter { plan, userSelected ->
            updateActionButtonAndGuidelineViews(plan)
        }
        layoutBinding.rvSubscription.adapter = subscriptionAdapter
        layoutBinding.rvSubscription.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        layoutBinding.ibBack.setOnClickListener {
            finish()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.subscriptionsState.collect { it ->
                        subscriptionAdapter.bestProductIdList =
                            RemoteConfigRepository.premiumPlans?.filter { it.isBest.orFalse() }
                                ?.map { it.productId.orEmpty() }
                        subscriptionAdapter.subscriptions = it
                        layoutBinding.scrollView.postDelayed(
                            {
                                if (layoutBinding.scrollView.canScrollVertically(1)) {
                                    layoutBinding.scrollView.fullScroll(View.FOCUS_DOWN)
                                }
                            },
                            200,
                        )
                    }
                }
                launch {
                    viewModel.screenState.collect {
                        when (it) {
                            ScreenState.ShowLoading -> showLoadingDialog("")
                            ScreenState.Purchasing -> showLoadingDialog(getString(R.string.processing))
                            ScreenState.HideLoading -> hideLoadingDialog()
                            ScreenState.PurchaseFailed -> {
                                subscriptionAdapter.productSelected?.productId?.let { it ->

                                }
                                showConfirmDialog(
                                    title = getString(R.string.dialog_purchase_failed_title),
                                    content = getString(R.string.dialog_purchase_failed_body),
                                    confirmCallback = {
                                    },
                                )
                            }
                            ScreenState.APIError -> {
                                Timber.tag("Premium").d("API ERROR")
                                //showAPIErrorPopup()
                            }
                        }
                    }
                }
            }
        }
        viewModel.planStateLiveData.observe(this) {
            when (it.plan) {
                PremiumPlanViewModel.Plan.STANDARD -> {
                    viewModel.querySubscriptions()
                    observePurchases()
                }
                PremiumPlanViewModel.Plan.PRO -> showProPlanContent(it.type, it.isLifetime)
            }
        }
        viewModel.checkProUserAndLoadPackage()
        viewModel.checkSubscriptionStatus()
        hideNavigationBar()
    }

    private fun updateActionButtonAndGuidelineViews(plan: BillingPlan) {
        val basePrice = plan.formattedBasePlanPrice
        if (plan.hasFreeTrial()) {
            layoutBinding.tvPurchaseGuideline.text = getString(
                R.string.premium_plan_free_trial_prompt,
                plan.freeTrialPeriod,
                basePrice + " " + plan.name.replaceFirstChar { c -> c.lowercase(Locale.getDefault()) },
            )
            layoutBinding.btnSubscribe.text = getString(R.string.premium_plan_subscribe_button_with_trial_label)
        } else if (!plan.isProduct) {
            layoutBinding.btnSubscribe.text = getString(R.string.premium_plan_subscribe_button_label)
            layoutBinding.tvPurchaseGuideline.text = getString(R.string.pricing_guideline, basePrice,
                plan.name.replace("Subscription", "")
                    .replaceFirstChar { c -> c.lowercase(Locale.getDefault()) })
        } else {
            layoutBinding.btnSubscribe.text = getString(R.string.premium_plan_purchase_button_label)
            layoutBinding.tvPurchaseGuideline.text =
                getString(R.string.premium_plan_one_time_purchase_subtitle)
        }
    }

    private fun showProPlanContent(type: String, isLifetime: Boolean) {
        layoutBinding.groupPlan.isVisible = false
        layoutBinding.tvPurchaseGuideline.isVisible = false
        layoutBinding.btnSubscribe.isVisible = false
        layoutBinding.groupPurchased.isVisible = true
        layoutBinding.tvCurrentSubscription.text = if (isLifetime) {
            getString(R.string.premium_plan_lifetime_title)
        } else {
            getString(R.string.premium_plan_current_subscription_label, type)
        }
        layoutBinding.tvNextBillingDate.text = getString(
            R.string.premium_plan_next_billing_date_label,
            SharedPreferUtils.subscriptionExpiredAt.toDateFormat(),
        )

    }

    private fun observePurchases() {
        launchAndRepeatOnLifecycleStarted {
            viewModel.purchasesState.collect { purchases ->
                if (purchases.isNotEmpty()) {
                    subscriptionAdapter.productSelected?.productId?.let { it ->

                    }
                    showConfirmDialog(
                        title = getString(R.string.dialog_purchase_success_title),
                        content = getString(R.string.dialog_purchase_success_body),
                        confirmCallback = {
                            if (intent.getStringExtra(ARG_SOURCE_KEY) in listOf(
                                    ARG_SOURCE_PRO_ELEMENT_KEY,
                                    ARG_SOURCE_EXPORT_KEY
                                )
                            ) {
                                setResult(
                                    Activity.RESULT_OK,
                                    Intent().apply {
                                        putExtra(ARG_SOURCE_KEY, intent.getStringExtra(ARG_SOURCE_KEY))
                                    },
                                )
                                finish()
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
    private fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }


    class PremiumBackDropAdapter : RecyclerView.Adapter<PremiumBackDropAdapter.ViewHolder>() {
        private val imageList = listOf(R.drawable.thumbnail_premium)

        class ViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView) {
            fun bindImage(drawableRes: Int) {
                (rootView as ImageView).setImageResource(drawableRes)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.view_img_item, parent, false),
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindImage(imageList[position])
        }

        override fun getItemCount() = imageList.size
    }

    companion object {
        internal const val ARG_SOURCE_KEY = "source"
        internal const val ARG_SOURCE_PRO_ELEMENT_KEY = "using_pro_element"
        internal const val ARG_SOURCE_EXPORT_KEY = "export_high_quality"
        fun newIntent(context: Context, source: String) =
            Intent(context, PremiumActivity::class.java).apply {
                putExtra(ARG_SOURCE_KEY, source)
            }
    }
}
