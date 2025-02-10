<div align="center">
<h1>SwitchFreeForm</h1>

![stars](https://img.shields.io/github/stars/HChenX/SwitchFreeForm?style=flat)
![downloads](https://img.shields.io/github/downloads/HChenX/SwitchFreeForm/total)
![Github repo size](https://img.shields.io/github/repo-size/HChenX/SwitchFreeForm)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/HChenX/SwitchFreeForm)](https://github.com/HChenX/SwitchFreeForm/releases)
[![GitHub Release Date](https://img.shields.io/github/release-date/HChenX/SwitchFreeForm)](https://github.com/HChenX/SwitchFreeForm/releases)
![last commit](https://img.shields.io/github/last-commit/HChenX/SwitchFreeForm?style=flat)
![language](https://img.shields.io/badge/language-java-purple)

[//]: # (<p><b><a href="README-en.md">English</a> | <a href="README.md">简体中文</a></b></p>)
<p>拖动小窗至全屏时自动将底层全屏应用切换为小窗模式</p>
</div>

# ✨模块介绍

- 拖动小窗至全屏时自动将底层全屏应用切换为小窗模式。
- 仅支持向下拖动展开的方式。

# 💡模块说明

- 仅在 `HyperOS2` 测试通过，理论不支持 `Miui` 系统。
- 安装后勾选`系统界面`并重启后即可使用。
- 触发自动切换方法：持续拖动小窗直到再次感觉到震动时松开即可，如果底部空间不足以触发再次震动则会在手指滑动至屏幕底部时直接震动进入切换模式。
- 同时支持设置 `setprop persist.hchen.switch.freeform.always true`，即可始终触发切换，设置 `false`
  禁用，默认禁用。
- 当然可以设置 `setprop persist.hchen.switch.freeform.threshold 800`，自定义敏感度。 Tip：800
  为默认值，只可设置数字！范围建议 500 - 900，数值越大需要滑动的距离越长。

- 建议的食用方法：

* 向下拉动小窗控制条直至再次感觉到震动，即可进行切换。
* 或者设置 `setprop persist.hchen.switch.freeform.always true`
  始终触发切换，同时使用向上拖动小窗至屏幕顶部来进行仅切换操作（因为拖动至顶部的切换逻辑未修改）。

- Debug 设置：
- 你可以设置 debug 来进行调试：
- 设置 `setprop persist.hchen.switch.freeform.debug.start.delay 450`
  来指定切换后启动小窗的延迟。用于在获取系统前台应用切换的回调后延迟启动小窗，数值太小可能动画冲突导致
  Bug。
- 设置 `setprop persist.hchen.switch.freeform.debug.ready.delay 600`
  来指定准备阶段的延迟。用于等待系统前台应用切换的回调，数值太小可能切换失败。

# 🙏致谢名单

- 本模块使用了如下项目作为依赖或引用其代码，对此表示由衷的感谢：

|   项目名称   |                      项目链接                      |
|:--------:|:----------------------------------------------:|
| HookTool | [HookTool](https://github.com/HChenX/HookTool) |
|   翻译提供   |                      提供者                       |
|   简体中文   |                    焕晨HChen                     |

# 📢项目声明

- 任何对本项目的使用必须注明作者，抄袭是不可接受的！
- 抄袭可能导致本项目的闭源！

# 🌏免责声明

- 使用本模块即代表愿意承担一切后果。
- 任何由本项目衍生出的项目本项目不承担任何责任。

# 🎉结尾

- 感谢您愿意使用本模块！Enjoy your day! ♥️
