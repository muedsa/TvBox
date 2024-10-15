/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material.icons.outlined

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Outlined.Remove: ImageVector
    get() {
        if (_remove != null) {
            return _remove!!
        }
        _remove = materialIcon(name = "Outlined.Remove") {
            materialPath {
                moveTo(19.0F, 13.0F)
                horizontalLineTo(5.0F)
                verticalLineToRelative(-2.0F)
                horizontalLineToRelative(14.0F)
                verticalLineToRelative(2.0F)
                close()
            }
        }
        return _remove!!
    }

private var _remove: ImageVector? = null
