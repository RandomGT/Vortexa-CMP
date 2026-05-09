package com.vortexa.ui.page.wallet.pay

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vortexa.ui.component.ClickableLinkText
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.URL_ANNOTATION_TAG

/** 涡联积分用户协议（占位链接，待与运营 H5 对齐后替换）。 */
const val POINT_RECHARGE_USER_AGREEMENT_URL = "https://www.vortexa.com/points-user-agreement"

/**
 * 协议勾选 + 可点击《涡联积分用户协议条款》打开链接。
 */
@Composable
fun PointRechargeAgreementRow(
    agreed: Boolean,
    onAgreedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val annotated = buildAnnotatedString {
        append("已阅读并同意")
        pushStringAnnotation(
            tag = URL_ANNOTATION_TAG,
            annotation = POINT_RECHARGE_USER_AGREEMENT_URL
        )
        withStyle(SpanStyle(color = Colors.blue_3266FF)) {
            append("《涡联积分用户协议条款》")
        }
        pop()
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = agreed,
            onCheckedChange = onAgreedChange,
            modifier = Modifier.padding(end = 0.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Colors.blue_3266FF,
                uncheckedColor = Colors.gray_B1B8C6
            )
        )
        ClickableLinkText(
            text = annotated,
            style = FontRegular(fontSize = 12, color = Colors.gray_6A7282)
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun PointRechargeAgreementRowPreview() {
    PointRechargeAgreementRow(agreed = true, onAgreedChange = {})
}
