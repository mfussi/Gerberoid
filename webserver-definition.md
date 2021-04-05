
# Gerberoid

A REST Api to remote control the gerberoid app. Load gerber and drill files, move, scale the display. Change visiblities.

## Indices

* [Ungrouped](#ungrouped)

  * [Clear](#1-clear)
  * [Fullscreen](#2-fullscreen)
  * [Layer Color](#3-layer-color)
  * [Layer Visibility](#4-layer-visibility)
  * [Load Drill](#5-load-drill)
  * [Load Gerber](#6-load-gerber)
  * [Move](#7-move)
  * [Overlay](#8-overlay)
  * [Scale](#9-scale)
  * [Zoom Fit](#10-zoom-fit)
  * [Zoom In](#11-zoom-in)
  * [Zoom Out](#12-zoom-out)


--------


## Ungrouped



### 1. Clear


Removes all layers from current view.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/clear
```



### 2. Fullscreen


Toggles the fullscreen. enable = true/false


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/fullscreen
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| enable | true | true / false |



### 3. Layer Color


Sets the layer colors. Color must be between 0 and 29, see definition of colors in source code. The layer index must be between 0 and 31.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/layer/color
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| color | 4 | gerberoid layer color index (0 - 28) |
| layer | 0 | layer index (0 - 31) |



### 4. Layer Visibility


Sets the layer visibility. Layer must be between 0 and 31.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/layer/visibility
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| visibility | true | visible (true) or hidden (false) |
| layer | 0 | layer index (0 - 31) |



### 5. Load Drill


Loads a new layer with a drill file. The gerber file needs to be provided as POST body. In binary format.


***Endpoint:***

```bash
Method: POST
Type: FILE
URL: http://{{host}}:6060/api/control/load/drill
```



### 6. Load Gerber


Loads a new layer with a gerber file. The gerber file needs to be provided as POST body. In binary format.


***Endpoint:***

```bash
Method: POST
Type: FILE
URL: http://{{host}}:6060/api/control/load/gerber
```



### 7. Move


Moves the viewport of the gerber frame. Given x and y coordinates (in pixels). The coordinates can be absolute or relative.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/move
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| x | 0 | move on x axis, value in pixels |
| y | 80 | move on y axis, value in pixels |
| absolute | false | given coordinates in absolute (true) or relative (false) values |



### 8. Overlay


Toggles overlay mode. If enabled an color correction overlay will be shown. enable = true/false


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/overlay
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| enable | false | true / false |



### 9. Scale


Sets the scale of the viewport of the gerber frame. Property scale can be provided in absolute or relative values.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/scale
```



***Query params:***

| Key | Value | Description |
| --- | ------|-------------|
| scale | 1.2 | scale factor (float) |
| absolute | false | given scale factor in absolute (true) or relative (false) value |



### 10. Zoom Fit


Zoom the viewport to best fit.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/zoom/fit
```



### 11. Zoom In


Zoom the viewport to the next zoom level.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/zoom/in
```



### 12. Zoom Out


Zoom the viewport to the previous zoom level.


***Endpoint:***

```bash
Method: POST
Type: 
URL: http://{{host}}:6060/api/control/zoom/out
```



---
[Back to top](#gerberoid)
> Made with &#9829; by [thedevsaddam](https://github.com/thedevsaddam) | Generated at: 2021-04-05 11:04:31 by [docgen](https://github.com/thedevsaddam/docgen)
