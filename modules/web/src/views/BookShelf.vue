<template>
  <div class="index-wrapper">
    <div class="navigation-wrapper">
      <div class="navigation-title-wrapper">
        <div class="navigation-title">阅读</div>
        <div class="navigation-sub-title">清风不识字，何故乱翻书</div>
      </div>
      <div class="search-wrapper">
        <el-input
          placeholder="搜索书籍，在线书籍自动加入书架"
          v-model="search"
          class="search-input"
          :prefix-icon="Search"
          @keyup.enter="searchBook"
        >
        </el-input>
      </div>
      <div class="bottom-wrapper">
        <div class="recent-wrapper">
          <div class="recent-title">最近阅读</div>
          <div class="reading-recent">
            <el-tag
              :type="readingRecent.name == '尚无阅读记录' ? 'warning' : ''"
              class="recent-book"
              size="large"
              @click="
                toDetail(
                  readingRecent.url,
                  readingRecent.name,
                  readingRecent.author,
                  readingRecent.chapterIndex,
                  readingRecent.chapterPos
                )
              "
              :class="{ 'no-point': readingRecent.url == '' }"
            >
              {{ readingRecent.name }}
            </el-tag>
          </div>
        </div>
        <div class="setting-wrapper">
          <div class="setting-title">基本设定</div>
          <div class="setting-item">
            <el-tag
              :type="connectType"
              size="large"
              class="setting-connect"
              :class="{ 'no-point': newConnect }"
              @click="setIP"
            >
              {{ connectStatus }}
            </el-tag>
          </div>
        </div>
      </div>
      <div class="bottom-icons">
        <a
          href="https://github.com/gedoor/legado_web_bookshelf"
          target="_blank"
        >
          <div class="bottom-icon">
            <img :src="githubUrl" alt="" />
          </div>
        </a>
      </div>
    </div>
    <div class="shelf-wrapper" ref="shelfWrapper">
      <book-items
        :books="books"
        @bookClick="handleBookClick"
        :isSearch="isSearching"
      ></book-items>
    </div>
  </div>
</template>

<script setup>
import "@/assets/fonts/shelffont.css";
import { useBookStore } from "@/store";
import githubUrl from "@/assets/imgs/github.png";
import { Search } from "@element-plus/icons-vue";
import loadingSvg from "@element-plus/icons-svg/loading.svg?raw";
import API from "@api";

const store = useBookStore();
const { connectStatus, connectType, newConnect, shelf } = storeToRefs(store);

const readingRecent = ref({
  name: "尚无阅读记录",
  author: "",
  url: "",
  chapterIndex: 0,
  chapterPos: 0,
});
const showLoading = ref(false);
const shelfWrapper = ref(null);
const loadingSerive = ref(null);
watch(showLoading, (loading) => {
  if (!loading) return loadingSerive.value?.close();
  loadingSerive.value = ElLoading.service({
    target: shelfWrapper.value,
    spinner: loadingSvg,
    text: "正在获取书籍信息",
    lock: true,
  });
});

const books = ref([]);
watchEffect(() => {
  if (books.value.length > 0) showLoading.value = false;
});

const search = ref("");
const isSearching = ref(false);
watchEffect(() => {
  if (isSearching.value && search.value != "") return;
  isSearching.value = false;
  books.value = [];
  if (search.value == "") {
    books.value = shelf.value;
    return;
  }
  books.value = shelf.value.filter((book) => {
    return (
      book.name.includes(search.value) || book.author.includes(search.value)
    );
  });
});

const searchBook = () => {
  if (search.value == "") return;
  books.value = [];
  store.clearSearchBooks();
  showLoading.value = true;
  isSearching.value = true;
  API.search(
    search.value,
    (data) => {
      try {
        store.setSearchBooks(JSON.parse(data));
        store.searchBooks.forEach((item) => books.value.push(item));
      } catch (e) {
        ElMessage.error("后端数据错误");
        throw e;
      }
    },
    () => {
      showLoading.value = false;
      if (books.value.length == 0) {
        ElMessage.info("搜索结果为空");
      }
    }
  );
};

const setIP = () => {};

const router = useRouter();
const handleBookClick = async (book) => {
  const {
    bookUrl,
    name,
    author,
    durChapterIndex = 0,
    durChapterPos = 0,
  } = book;
  await API.saveBook(book);
  toDetail(bookUrl, name, author, durChapterIndex, durChapterPos);
};
const toDetail = (bookUrl, bookName, bookAuthor, chapterIndex, chapterPos) => {
  if (bookName === "尚无阅读记录") return;
  sessionStorage.setItem("bookUrl", bookUrl);
  sessionStorage.setItem("bookName", bookName);
  sessionStorage.setItem("bookAuthor", bookAuthor);
  sessionStorage.setItem("chapterIndex", chapterIndex);
  sessionStorage.setItem("chapterPos", chapterPos);
  readingRecent.value = {
    name: bookName,
    author: bookAuthor,
    url: bookUrl,
    chapterIndex: chapterIndex,
    chapterPos: chapterPos,
  };
  localStorage.setItem("readingRecent", JSON.stringify(readingRecent.value));
  router.push({
    path: "/chapter",
  });
};

onMounted(async () => {
  //获取最近阅读书籍
  let readingRecentStr = localStorage.getItem("readingRecent");
  if (readingRecentStr != null) {
    readingRecent.value = JSON.parse(readingRecentStr);
    if (typeof readingRecent.value.chapterIndex == "undefined") {
      readingRecent.value.chapterIndex = 0;
    }
  }
  showLoading.value = true;
  await store.saveBookProcess();
  fetchBookShelfData();
});
const fetchBookShelfData = () => {
  API.getBookShelf()
    .then((response) => {
      store.setConnectType("success");
      if (response.data.isSuccess) {
        //store.increaseBookNum(response.data.data.length);
        store.addBooks(
          response.data.data.sort(function (a, b) {
            var x = a["durChapterTime"] || 0;
            var y = b["durChapterTime"] || 0;
            return y - x;
          })
        );
      } else {
        ElMessage.error(response.data.errorMsg);
        showLoading.value = false;
      }
      store.setConnectStatus("已连接 ");
      store.setNewConnect(false);
    })
    .catch(function (error) {
      showLoading.value = false;
      store.setConnectType("danger");
      store.setConnectStatus("连接失败");
      ElMessage.error("后端连接失败");
      store.setNewConnect(false);
      throw error;
    });
};
</script>

<style lang="scss" scoped>
.index-wrapper {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: row;

  .navigation-wrapper {
    width: 260px;
    min-width: 260px;
    padding: 48px 36px;
    background-color: #f7f7f7;

    .navigation-title {
      font-size: 24px;
      font-weight: 500;
      font-family: FZZCYSK;
    }

    .navigation-sub-title {
      font-size: 16px;
      font-weight: 300;
      font-family: FZZCYSK;
      margin-top: 16px;
      color: #b1b1b1;
    }

    .search-wrapper {
      .search-input {
        border-radius: 50%;
        margin-top: 24px;

        :deep(.el-input__wrapper) {
          border-radius: 50px;
          border-color: #e3e3e3;
        }
      }
    }

    .recent-wrapper {
      margin-top: 36px;

      .recent-title {
        font-size: 14px;
        color: #b1b1b1;
        font-family: FZZCYSK;
      }

      .reading-recent {
        margin: 18px 0;

        .recent-book {
          font-size: 10px;
          // font-weight: 400;
          // margin: 12px 0;
          // font-weight: 500;
          // color: #6B7C87;
          cursor: pointer;
          // padding: 6px 18px;
        }
      }
    }

    .setting-wrapper {
      margin-top: 36px;

      .setting-title {
        font-size: 14px;
        color: #b1b1b1;
        font-family: FZZCYSK;
      }

      .no-point {
        pointer-events: none;
      }

      .setting-connect {
        font-size: 8px;
        margin-top: 16px;
        // color: #6B7C87;
        cursor: pointer;
      }
    }

    .bottom-icons {
      position: fixed;
      bottom: 0;
      height: 120px;
      width: 260px;
      align-items: center;
      display: flex;
      flex-direction: row;
    }
  }

  .shelf-wrapper {
    padding: 48px 48px;
    width: 100%;
    display: flex;
    flex-direction: column;
    :deep(.el-loading-mask) {
      background-color: rgba(0, 0, 0, 0);
    }
    :deep(.el-loading-spinner) {
      font-size: 36px;
      color: #b5b5b5;
    }

    :deep(.el-loading-text) {
      font-weight: 500;
      color: #b5b5b5;
    }
  }
}

@media screen and (max-width: 750px) {
  .index-wrapper {
    overflow-x: hidden;
    flex-direction: column;
    .navigation-wrapper {
      padding: 20px 24px;
      box-sizing: border-box;
      width: 100%;
      .navigation-title-wrapper {
        white-space: nowrap;
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
      }
      .bottom-wrapper,
      .bottom-icons {
        display: none;
      }
    }
    .shelf-wrapper {
      padding: 0;
      :deep(.el-loading-spinner) {
        display: none;
      }
    }
  }
}
</style>
