Gerberoid
=========

Gerberoid is an Android port of the gerber file viewer GerbView, which
is part of the [KiCad][1] EDA suite.

<a href="https://f-droid.org/packages/se.pp.mc.android.Gerberoid/" target="_blank">
<img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80"/></a>

Webserver
-------
This Gerberoid Fork can be remote controlled using an REST API. See the [documentation](webserver-definition.md).
Simply replace http://{{host}}}:6060 with the IP of your android device, e.g.: http://192.168.0.1:6060

For example - enable fullscreen mode:
[POST] http://192.168.0.1:6060/api/control/fullscreen?enable=true

License
-------

Copyright (C) 2017 Marcus Comstedt
Copyright (C) 1992-2015 KiCad Developers Team
Copyright (C) 2011-2013 Paul Burke

Gerberoid is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gerberoid is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gerberoid.  If not, see <http://www.gnu.org/licenses/>.

---

[1]: http://kicad-pcb.org/
