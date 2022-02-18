package com.iamport.sdk.data.sdk

import android.os.Parcelable
import com.iamport.sdk.domain.utils.Util
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * SDK 에 결제 요청할 데이터
 * https://docs.iamport.kr/tech/imp?lang=ko#param
 */

@Parcelize
data class IamPortRequest(
    val pg: String,
    val pay_method: String = PayMethod.card.name, // default card
    val escrow: Boolean? = null, // default false
    val merchant_uid: String, // default "random"
    val customer_uid: String? = null, // 정기결제용
    val name: String? = null,
    val amount: String,
    @Transient
    val custom_data: String? = null,
    val tax_free: Float? = null,
    val currency: String? = null, // default KRW, 페이팔은 USD 이어야 함
    val language: String? = null, // default "ko", en, zh, jp => TODO validator 에 추가
    val buyer_name: String? = null,
    val buyer_tel: String? = null,
    val buyer_email: String? = null,
    val buyer_addr: String? = null,
    val buyer_postcode: String? = null,
    val notice_url: List<String>? = null,
    val display: CardQuota? = null,
    val digital: Boolean? = null, // default false
    val vbank_due: String? = null, // YYYYMMDDhhmm
//    private var m_redirect_url: String? = Platform.native.redirectUrl, // 콜백
    val app_scheme: String? = null,
    val biz_num: String? = null,
    val popup: Boolean? = null,
//    private val niceMobileV2 : Boolean = true,

    // 네이버 관련
    val naverPopupMode: Boolean? = null,
    val naverUseCfm: String? = null,
    val naverProducts: List<BaseProductItem>? = null,
    val naverCultureBenefit: Boolean? = null,
    val naverProductCode: String? = null,
    val naverActionType: String? = null,

    val cultureBenefit: Boolean? = null,
    val naverInterface: NaverInterface? = null,

    val confirm_url: String? = null, // 컨펌프로세스
    val card: Card? = null, // 카드사 다이렉트 호출

) : Parcelable {

    private var m_redirect_url: String? = Platform.native.redirectUrl // 콜백
    private val niceMobileV2: Boolean = true

    /**
     * string pg 으로 enum PG 가져옴
     */
    @IgnoredOnParcel
    val pgEnum: PG?
        get() {
            return PG.convertPG(pg)
        }

    @IgnoredOnParcel
    var platform: String? = null
        set(value) {
            value?.let { it ->
                Platform.convertPlatform(it)?.let { p ->
                    m_redirect_url = p.redirectUrl
                } ?: run {
                    m_redirect_url = Util.getRedirectUrl(it)
                }
            }
            field = null
        }
        get() {
            return null
        }


    companion object {
        @JvmStatic
        fun builder() = Builder()

        class Builder {
            lateinit var pg: String
            lateinit var merchant_uid: String
            lateinit var amount: String

            var pay_method: String = PayMethod.card.name // 명세상 필수인지 불명확함, default card
            var escrow: Boolean? = null // default false

            var customer_uid: String? = null
            var name: String? = null

            var custom_data: String? = null // 명세상 불명확
            var tax_free: Float? = null
            var currency: String? = null // default KRW, 페이팔은 USD 이어야 함
            var language: String? = null // default "ko"
            var buyer_name: String? = null
            var buyer_tel: String? = null
            var buyer_email: String? = null
            var buyer_addr: String? = null
            var buyer_postcode: String? = null
            var notice_url: List<String>? = null
            var display: CardQuota? = null
            var digital: Boolean? = null // default false
            var vbank_due: String? = null // YYYYMMDDhhmm
            private var m_redirect_url: String? = Platform.native.redirectUrl // 콜백
            var app_scheme: String? = null // 명세상 nullable 이나, RN 에서 필수
            var biz_num: String? = null
            var popup: Boolean? = null // 명세상 없으나, RN 에 있음
            private val niceMobileV2: Boolean = true

            // 네이버 관련
            var naverPopupMode: Boolean? = null
            var naverUseCfm: String? = null
            var naverProducts: List<BaseProductItem>? = null

            var naverCultureBenefit: Boolean? = null
            var naverProductCode: String? = null
            var naverActionType: String? = null

            var cultureBenefit: Boolean? = null
            var naverInterface: NaverInterface? = null

            var confirm_url: String? = null

            fun pg(pg: String) = apply {
                this.pg = pg
            }

            fun pay_method(pay_method: String) = apply {
                this.pay_method = pay_method
            }

            fun escrow(escrow: Boolean) = apply {
                this.escrow = escrow
            }

            fun merchant_uid(merchant_uid: String) = apply {
                this.merchant_uid = merchant_uid
            }

            fun name(name: String) = apply {
                this.name = name
            }

            fun customer_uid(customer_uid: String) = apply {
                this.customer_uid = customer_uid
            }

            fun amount(amount: String) = apply {
                this.amount = amount
            }

            fun custom_data(custom_data: String) = apply {
                this.custom_data = custom_data
            }

            fun tax_free(tax_free: Float) = apply {
                this.tax_free = tax_free
            }

            fun currency(currency: String) = apply {
                this.currency = currency
            }

            fun language(language: String) = apply {
                this.language = language
            }

            fun buyer_name(buyer_name: String) = apply {
                this.buyer_name = buyer_name
            }

            fun buyer_tel(buyer_tel: String) = apply {
                this.buyer_tel = buyer_tel
            }

            fun buyer_email(buyer_email: String) = apply {
                this.buyer_email = buyer_email
            }

            fun buyer_addr(buyer_addr: String) = apply {
                this.buyer_addr = buyer_addr
            }

            fun buyer_postcode(buyer_postcode: String) = apply {
                this.buyer_postcode = buyer_postcode
            }

            fun notice_url(notice_url: List<String>) = apply {
                this.notice_url = notice_url
            }

            fun display(display: CardQuota) = apply {
                this.display = display
            }

            fun digital(digital: Boolean) = apply {
                this.digital = digital
            }

            fun vbank_due(vbank_due: String) = apply {
                this.vbank_due = vbank_due
            }

//            private fun m_redirect_url(m_redirect_url: String) = apply {
//                this.m_redirect_url = m_redirect_url
//            }

            fun app_scheme(app_scheme: String) = apply {
                this.app_scheme = app_scheme
            }

            fun biz_num(biz_num: String) = apply {
                this.biz_num = biz_num
            }

            fun popup(popup: Boolean) = apply {
                this.popup = popup
            }

            fun naverPopupMode(naverPopupMode: Boolean) = apply {
                this.naverPopupMode = naverPopupMode
            }

            fun naverUseCfm(naverUseCfm: String) = apply {
                this.naverUseCfm = naverUseCfm
            }

            fun naverProducts(naverProducts: List<BaseProductItem>) = apply {
                this.naverProducts = naverProducts
            }

            fun naverCultureBenefit(naverCultureBenefit: Boolean) = apply {
                this.naverCultureBenefit = naverCultureBenefit
            }

            fun naverProductCode(naverProductCode: String) = apply {
                this.naverProductCode = naverProductCode
            }

            fun naverActionType(naverActionType: String) = apply {
                this.naverActionType = naverActionType
            }

            fun cultureBenefit(cultureBenefit: Boolean) = apply {
                this.cultureBenefit = cultureBenefit
            }

            fun naverInterface(naverInterface: NaverInterface) = apply {
                this.naverInterface = naverInterface
            }

            fun confirm_url(confirm_url: String) = apply {
                this.confirm_url = confirm_url
            }


            fun build() = IamPortRequest(
                pg = pg,
                pay_method = pay_method,
                escrow = escrow,
                merchant_uid = merchant_uid,
                customer_uid = customer_uid,
                name = name,
                amount = amount,
                custom_data = custom_data,
                tax_free = tax_free,
                currency = currency,
                language = language,
                buyer_name = buyer_name,
                buyer_tel = buyer_tel,
                buyer_email = buyer_email,
                buyer_addr = buyer_addr,
                buyer_postcode = buyer_postcode,
                notice_url = notice_url,
                display = display,
                digital = digital,
                vbank_due = vbank_due,
                app_scheme = app_scheme,
                biz_num = biz_num,
                popup = popup,
                naverPopupMode = naverPopupMode,
                naverUseCfm = naverUseCfm,
                naverProducts = naverProducts,
                naverCultureBenefit = naverCultureBenefit,
                naverProductCode = naverProductCode,
                naverActionType = naverActionType,
                cultureBenefit = cultureBenefit,
                naverInterface = naverInterface,
                confirm_url = confirm_url
            )
        }
    }
}

