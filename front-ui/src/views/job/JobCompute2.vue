<template>
  <div class="entry-container" ref="screenRef">
    <div class="mapBox">
      <div id="mapBody"></div>
    </div>
    <div id="minimap"></div>
    <TeleportContainer />  // 很重要，必须将TeleportContainer标签放置在html结构中
  </div>
  </div>
</template>

<script setup>
import { register, getTeleport } from "@antv/x6-vue-shape/lib";
import { Graph, Path, Edge, Platform } from "@antv/x6";
import { Transform } from "@antv/x6-plugin-transform";
import { Selection } from "@antv/x6-plugin-selection";
import { Snapline } from "@antv/x6-plugin-snapline";
import { Keyboard } from "@antv/x6-plugin-keyboard";
import { Clipboard } from "@antv/x6-plugin-clipboard";
import { History } from "@antv/x6-plugin-history";
import { Scroller } from "@antv/x6-plugin-scroller";
import { Dnd } from "@antv/x6-plugin-dnd";
import { MiniMap } from "@antv/x6-plugin-minimap";
</script>