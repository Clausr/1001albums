package dk.clausr.feature.overview.notifications

import android.content.Context
import dk.clausr.core.model.Notification
import dk.clausr.core.model.NotificationData
import dk.clausr.feature.overview.R

fun Notification.getTitle(context: Context): String? {
    return when (data) {
        is NotificationData.AlbumsRatedData -> context.getString(R.string.notification_albums_rated_title)
        is NotificationData.GroupAlbumsGeneratedData -> context.getString(R.string.notification_group_albums_generated_title)
        is NotificationData.GroupReviewData -> context.getString(R.string.notification_group_review_title)
        is NotificationData.NewGroupMemberData -> context.getString(R.string.notification_new_group_member_title)
        is NotificationData.ReviewThumbUpData -> context.getString(R.string.notification_thumb_up_title)
        null -> null
    }
}

fun Notification.getBody(context: Context): String? {
    return when (val data = data) {
        is NotificationData.AlbumsRatedData -> context.getString(R.string.notification_albums_rated_body, data.numberOfAlbums)
        is NotificationData.GroupAlbumsGeneratedData -> context.getString(R.string.notification_group_albums_generated_body, data.numberOfAlbums)
        is NotificationData.GroupReviewData ->
            context.getString(R.string.notification_group_review_body, data.projectName, data.albumName, data.rating)

        is NotificationData.NewGroupMemberData -> context.getString(R.string.notification_new_group_member_body)
        is NotificationData.ReviewThumbUpData -> context.getString(R.string.notification_thumb_up_body, data.albumName, data.thumbsUp)
        null -> null
    }
}