package com.themavericklabs.fleetly.data.models

import com.google.gson.annotations.SerializedName

data class GraphResponse<T>(
    @SerializedName("@odata.context")
    val context: String?,
    @SerializedName("@odata.count")
    val count: Int?,
    @SerializedName("value")
    val value: T,
    @SerializedName("@odata.nextLink")
    val nextLink: String?
)

data class GraphError(
    val error: GraphErrorDetail
)

data class GraphErrorDetail(
    val code: String,
    val message: String
)