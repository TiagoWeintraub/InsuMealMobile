package com.insumeal.schemas

import com.google.gson.annotations.SerializedName

data class PaginationMetadataSchema(
    @SerializedName("page") val page: Int,
    @SerializedName("page_size") val pageSize: Int,
    @SerializedName("total_items") val totalItems: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("has_next") val hasNext: Boolean,
    @SerializedName("has_previous") val hasPrevious: Boolean
)

data class PaginatedResponseSchema<T>(
    @SerializedName("items") val items: List<T>,
    @SerializedName("pagination") val pagination: PaginationMetadataSchema
)

