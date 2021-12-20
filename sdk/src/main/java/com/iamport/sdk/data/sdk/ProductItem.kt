package com.iamport.sdk.data.sdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * for naverpay
 */
@Parcelize
open class BaseProductItem : Parcelable

// 결제형
// https://github.com/iamport/iamport-manual/blob/master/NAVERPAY/sample/naverpay-pg.md#2-naverproducts-%ED%8C%8C%EB%9D%BC%EB%A9%94%ED%84%B0
// https://developer.pay.naver.com/docs/v1/api/payments
@Parcelize
data class ProductItem(
    val categoryType: String,
    val categoryId: String,
    val uid: String,
    val name: String,
    val payReferrer: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val sellerId: String? = null,
    val count: Int? = null,
) : Parcelable, BaseProductItem()

// 주문형
// https://github.com/iamport/iamport-manual/blob/master/NAVERPAY/sample/README.md#21-paramnaverproducts-%EC%9D%98-%EA%B5%AC%EC%A1%B0
@Parcelize
data class ProductItemForOrder(
    val id: String,   //상품고유ID
    val merchantProductId: String? = null, //상품관리ID(필요한 경우만 선언. 정의하지 않으면 id값과 동일한 값을 자동 적용합니다)
    val ecMallProductId: String? = null,   //지식쇼핑상품관리ID(필요한 경우만 선언. 정의하지 않으면 id값과 동일한 값을 자동 적용합니다)
    val name: String, //상품명
    val basePrice: Int, //상품가격
    val taxType: String? = null,       //부가세 부과 여부(TAX or TAX_FREE)
    val quantity: Int,  //상품구매수량
    val infoUrl: String,    //상품상세페이지 URL
    val imageUrl: String,   //상품 Thumbnail 이미지 URL
    val giftName: String? = null, //해당상품 구매시 제공되는 사은품 명칭(없으면 정의하지 않음)
    val options: List<Option>? = null,     //구매자가 선택한 상품 옵션에 대한 상세 정보
    val supplements: List<Supplement>? = null,
    val shipping: Shipping? = null,    //상품 배송관련 상세 정보
) : Parcelable, BaseProductItem()


// 확실치 않은 정보들은 nullable 처리.. 어차피 네이버가 따로 검수!
@Parcelize
data class Option(
    val optionQuantity: Int,
    val optionPrice: Int? = null,
    val selectionCode: String? = null,
    val selections: List<Selection>? = null,
) : Parcelable


@Parcelize
data class Selection(
    val code: String? = null,
    val label: String? = null,
    val value: String? = null,
) : Parcelable


@Parcelize
data class Supplement(
    val id: String? = null,    //추가구성품의 ID
    val name: String? = null,    //추가구성품 상품명
    val price: Int? = null,           //추가구성품 가격
    val quantity: Int? = null            //추가구성품 수량
) : Parcelable


@Parcelize
data class Shipping(
    val groupId: String? = null,
    val method: String? = null,
    val baseFee: Int? = null,
    val feeRule: List<FeeRule>? = null,
    val feePayType: String? = null,
) : Parcelable


@Parcelize
data class FeeRule(
    val freeByThreshold: Int? = null,
) : Parcelable

