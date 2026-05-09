package com.vortexa.ui.page.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vortexa.ui.theme.Colors
import com.vortexa.ui.theme.FontRegular
import com.vortexa.util.extension.click
import vortexa.composeapp.generated.resources.Res

/**
 * desc : Shared Authentication Components
 *
 * @author LuXin
 * @createTime 2026/1/21
 */

@Composable
fun AuthInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = FontRegular(16, Colors.black_101828),
        singleLine = true,
        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .size(width = 315.dp, height = 50.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Colors.gray_F3F5F7)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = FontRegular(16, Colors.gray_B1B8C6)
                        )
                    }
                    innerTextField()
                }

                if (trailingContent != null) {
                    trailingContent()
                } else if (isPassword && onTogglePasswordVisibility != null) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Image(
                        painter = painterResource(
                            if (isPasswordVisible)  Res.drawable.see else Res.drawable.unseen
                        ),
                        contentDescription = if (isPasswordVisible) "隐藏密码" else "显示密码",
                        modifier = Modifier
                            .size(24.dp)
                            .click(onTogglePasswordVisibility)
                    )
                }
            }
        }
    )
}
