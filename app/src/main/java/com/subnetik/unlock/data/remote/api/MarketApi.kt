package com.subnetik.unlock.data.remote.api

import com.subnetik.unlock.data.remote.dto.admin.CouponValidateRequest
import com.subnetik.unlock.data.remote.dto.admin.CouponValidateResponse
import com.subnetik.unlock.data.remote.dto.admin.CouponRedeemRequest
import com.subnetik.unlock.data.remote.dto.admin.CouponRedeemResponse
import com.subnetik.unlock.data.remote.dto.market.MarketItemResponse
import com.subnetik.unlock.data.remote.dto.market.MarketPurchaseRequest
import com.subnetik.unlock.data.remote.dto.market.MarketPurchaseResponse
import com.subnetik.unlock.data.remote.dto.student.StudentTokenWallet
import retrofit2.http.*

interface MarketApi {
    @GET("homework/my/wallet")
    suspend fun getWallet(): StudentTokenWallet

    @GET("homework/market/items")
    suspend fun getMarketItems(): List<MarketItemResponse>

    @GET("homework/market/purchases")
    suspend fun getMyPurchases(): List<MarketPurchaseResponse>

    @POST("homework/market/purchase")
    suspend fun buyItem(@Body request: MarketPurchaseRequest): MarketPurchaseResponse

    // Coupon endpoints
    @POST("promocodes/validate")
    suspend fun validateCoupon(@Body request: CouponValidateRequest): CouponValidateResponse

    @POST("promocodes/redeem")
    suspend fun redeemCoupon(@Body request: CouponRedeemRequest): CouponRedeemResponse
}
