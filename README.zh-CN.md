# theme

[English](README.md)

独立的 Norm 主题基础库。一个必填的种子色即可派生明暗主题、前景与背景、边框和控件交互配色。UI 库只消费 `ThemeSource`，应用通过 `ThemeManager` 管理主题。

核心不依赖任何 UI 框架，只使用 Norm 标准库与本仓库的 JDK 颜色内核。Swing 仅用于独立桌面示例。线程调度、渲染、动画、系统偏好和配置持久化由接入方负责。

- [完整使用示例](samples/basic/application.norm)
- [UI 适配契约](docs/adapters.md)
- [颜色类型](theme/color.norm)
- [配色 API](theme/palette.norm)
- [主题生命周期 API](theme/lifecycle.norm)
- [主题测试](theme/tests/lifecycle_test.norm)
- [构建与本地包验证](README.md#development)
- [真实桌面示例](samples/README.md)

模块名称、版本、依赖和颜色内核坐标统一在 [module.norm](theme/module.norm) 声明。构建使用独立缓存，不会发布工件或修改用户的常规包缓存。
