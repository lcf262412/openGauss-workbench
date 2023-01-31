/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.plugin.vo.modeling.component;

import java.util.List;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class Bmap {

    private String key;
    private List<Double> center;
    private int zoom;
    private boolean roam;
    private MapStyle mapStyle;

    public Bmap setKey(String key) {
        this.key = key;
        return this;
    }
    public String getKey() {
        return key;
    }

    public Bmap setCenter(List<Double> center) {
        this.center = center;
        return this;
    }
    public List<Double> getCenter() {
        return center;
    }

    public Bmap setZoom(int zoom) {
        this.zoom = zoom;
        return this;
    }
    public int getZoom() {
        return zoom;
    }

    public Bmap setRoam(boolean roam) {
        this.roam = roam;
        return this;
    }
    public boolean getRoam() {
        return roam;
    }

    public Bmap setMapStyle(MapStyle mapStyle) {
        this.mapStyle = mapStyle;
        return this;
    }
    public MapStyle getMapStyle() {
        return mapStyle;
    }

}