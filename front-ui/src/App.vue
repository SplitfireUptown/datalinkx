<template>
  <a-config-provider :locale="locale">
    <div id="app">
      <router-view/>
      <div class="chat-container">
        <beautiful-chat
          :participants="participants"
          :titleImageUrl="titleImageUrl"
          :onMessageWasSent="onMessageWasSent"
          :messageList="messageList"
          :newMessagesCount="newMessagesCount"
          :isOpen="isChatOpen"
          :close="closeChat"
          :open="openChat"
          :showEmoji="false"
          :showFile="false"
          :showEdition="true"
          :showDeletion="true"
          :showTypingIndicator="showTypingIndicator"
          :showLauncher="true"
          :showCloseButton="true"
          :colors="colors"
          :alwaysScrollToBottocom="alwaysScrollToBottom"
          :disableUserListToggle="false"
          :messageStyling="messageStyling"
          @onType="handleOnType"
          @edit="editMessage"
        >
          <template v-slot:header>
            <!-- 添加 position: relative 作为定位基准 -->
            <div style="pointer-events: auto; z-index: 10001; overflow: visible; position: relative;">
              <a-select v-model="chatMode" default-value="qa" style="width: 120px" @change.capture="handleChange">
                <a-select-option value="qa">智能问答</a-select-option>
                <a-select-option value="command">指令模式</a-select-option>
              </a-select>
            </div>
          </template>
          <template v-slot:system-message-body="{ message }">
            [System]: {{message.text}}
          </template>
        </beautiful-chat>
      </div>
    </div>
  </a-config-provider>
</template>

<script>
import { domTitle, setDocumentTitle } from '@/utils/domUtil'
import { i18nRender } from '@/locales'
import EVA from '@/assets/eva.png'
import TitleImg from '@/assets/titleImg.png'

export default {
  data () {
    return {
      msg: '聊天浮球',
      // 修正初始值为选项的 value（'qa'）
      chatMode: 'qa',
      participants: [
        {
          id: 'user1',
          name: 'Matteo',
          imageUrl: EVA
        }
      ], // the list of all the participant of the conversation. `name` is the user name, `id` is used to establish the author of a message, `imageUrl` is supposed to be the user avatar.
      titleImageUrl: TitleImg,
      messageList: [
        // { type: 'text', author: `me`, data: { text: `Say yes!` } },
        { type: 'text', author: `user1`, data: { text: `你好，我是DatalinkX智能问答助手` } }
      ], // the list of the messages to show, can be paginated and adjusted dynamically
      newMessagesCount: 0,
      isChatOpen: false, // to determine whether the chat window should be open or closed
      showTypingIndicator: '', // when set to a value matching the participant.id it shows the typing indicator for the specific user
      colors: {
        header: {
          bg: '#4e8cff',
          text: '#ffffff'
        },
        launcher: {
          bg: '#4e8cff'
        },
        messageList: {
          bg: '#ffffff'
        },
        sentMessage: {
          bg: '#4e8cff',
          text: '#ffffff'
        },
        receivedMessage: {
          bg: '#eaeaea',
          text: '#222222'
        },
        userInput: {
          bg: '#f4f7f9',
          text: '#565867'
        }
      }, // specifies the color scheme for the component
      alwaysScrollToBottom: false, // when set to true always scrolls the chat to the bottom when new events are in (new message, user starts typing...)
      messageStyling: true // enables *bold* /emph/ _underline_ and such (more info at github.com/mattezza/msgdown)
    }
  },
  methods: {
    onMessageWasSent (message) {
      // called when the user sends a message
      this.messageList = [ ...this.messageList, message ]
      const lastChild = document.querySelector('.sc-message-list').lastElementChild
      lastChild.style.display = 'block'

      let targetUrl = 'api/api/copilot/stream/chat'
      if (this.chatMode === 'command') {
        targetUrl = 'api/api/mcp/stream/chat'
      }

      const eventSource = new EventSource(targetUrl + '?question=' + message.data.text)
      eventSource.onopen = function (event) {
        console.log(event.data)
      }
      const answerId = this.getTimestamp()
      var self = this
      eventSource.onmessage = function (event) {
        var modelMessage = JSON.parse(event.data)

        console.log(answerId)
        let flag = 0
        for (const message of self.messageList) {
           if (message.id === answerId) {
             flag = 1
             if (this.chatMode === 'qa') {
               console.log(message.data.text)
               message.data.text = message.data.text.concat(modelMessage.message.content)
             } else {
               if (modelMessage.result === null || modelMessage.result === '<think>' || modelMessage.result === '</think>') {
                 continue
               }
               message.data.text = message.data.text.concat(modelMessage.result)
             }
           }
        }
        if (flag === 0) {
          let text = ''
          if (this.chatMode === 'qa') {
            text = modelMessage.message.content
          } else {
            text = modelMessage.result
          }
          console.log(text)
          const answer = {
            type: 'text',
            author: `user1`,
            id: answerId,
            data: {
              text: text
            }
          }
          self.messageList.push(answer)
        }
      }
      eventSource.onerror = function (error) {
        console.error('Error:', error)
        lastChild.style.display = 'none'
        eventSource.close()
      }
      // eventSource.close()
    },
    handleChange (value) {
      console.log(value)
    },
    openChat () {
      // called when the user clicks on the fab button to open the chat
      this.isChatOpen = true
      this.newMessagesCount = 0
    },
    closeChat () {
      // called when the user clicks on the botton to close the chat
      this.isChatOpen = false
    },
    handleScrollToTop () {
      // called when the user scrolls message list to top
      // leverage pagination for loading another page of messages
    },
    handleOnType () {
      console.log('Emit typing event')
    },
    editMessage (message) {
      const m = this.messageList.find(m => m.id === message.id)
      m.isEdited = true
      m.data.text = message.data.text
    },
    getTimestamp () {
      return new Date().getTime() + Math.random()
    }
  },
  computed: {
    locale () {
      // 只是为了切换语言时，更新标题
      const { title } = this.$route.meta
      title && (setDocumentTitle(`${i18nRender(title)} - ${domTitle}`))

      return this.$i18n.getLocaleMessage(this.$store.getters.lang).antLocale
    }
  }
}
</script>

<style scoped>
h1, h2 {
  font-weight: normal;
}
ul {
  list-style-type: none;
  padding: 0;
}
li {
  display: inline-block;
  margin: 0 10px;
}
a {
  color: #42b983;
}
.chat-container {
  position: relative;
  z-index: 9999;
}
  .select-box {
    margin: 10px;
    padding: 5px;
    width: 150px;
    border: 1px solid #4e8cff;
    border-radius: 4px;
    position: absolute; /* 添加绝对定位 */
    top: 10px; /* 距离顶部10px */
    right: 10px; /* 距离右侧10px */
    z-index: 10000; /* 确保层级高于聊天容器 */
  }
</style>

<style>
/* 非 scoped 样式，直接覆盖全局类 */
.ant-select-dropdown {
  z-index: 10002 !important;
}

/* 优化 a-select 整体样式 */
.ant-select {
  font-size: 14px; /* 调整字体大小 */
}

/* 输入框样式（未聚焦） */
.ant-select-selector {
  height: 36px !important; /* 固定高度 */
  line-height: 36px !important;
  border: 1px solid #e5e7eb !important; /* 柔和的边框颜色 */
  border-radius: 8px !important; /* 增大圆角 */
  padding: 0 12px !important; /* 调整内间距 */
  transition: all 0.2s ease; /* 动画过渡 */
}

/* 输入框悬停样式 */
.ant-select-selector:hover {
  border-color: #4e8cff !important; /* 悬停时边框颜色（项目主色） */
}

/* 输入框聚焦样式 */
.ant-select-focused .ant-select-selector {
  border-color: #4e8cff !important; /* 聚焦时边框颜色 */
  box-shadow: 0 0 0 2px rgba(78, 140, 255, 0.1) !important; /* 柔和的阴影 */
}

/* 下拉箭头样式 */
.ant-select-arrow {
  color: #4e8cff !important; /* 箭头颜色与主色一致 */
  font-size: 16px !important; /* 增大箭头大小 */
}

/* 选项列表样式（可选，根据需求调整） */
.ant-select-dropdown .ant-select-item {
  padding: 8px 12px; /* 选项内间距 */
  border-radius: 4px; /* 选项圆角 */
}

/* 选项悬停/选中样式 */
.ant-select-item:hover,
.ant-select-item-selected {
  background-color: rgba(78, 140, 255, 0.05) !important; /* 主色浅背景 */
  color: #4e8cff !important; /* 文字颜色 */
}
</style>
