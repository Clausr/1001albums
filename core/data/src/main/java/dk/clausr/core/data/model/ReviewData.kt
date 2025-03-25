package dk.clausr.core.data.model

import dk.clausr.core.model.GroupReview

data class ReviewData(
    val reviews: List<GroupReview> = emptyList(),
    val isLoading: Boolean,
)
