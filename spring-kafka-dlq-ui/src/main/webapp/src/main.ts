import Vue from 'vue';

/* ============
 * Axios
 * ============
 *
 * Promise based HTTP client for the browser and node.js.
 * Because Vue Resource has been retired, Axios will now been used
 * to perform AJAX-requests.
 *
 * @see https://github.com/mzabriskie/axios
 */
import Axios from 'axios';

Axios.defaults.baseURL = 'http://localhost:8888';
Axios.defaults.headers.common['Content-Type'] = 'application/json';
Axios.defaults.headers.common['Accept'] = 'application/json';

/* ============
 * Notifications
 * ============
 *
 * @see https://github.com/euvl/vue-notification
 */
import Notifications from 'vue-notification';

Vue.use(Notifications);

/* ============
 * Vue Good Table
 * ============
 *
 * @see https://github.com/xaksis/vue-good-table
 */
// @ts-ignore
import VueGoodTable from 'vue-good-table';
// @ts-ignore
import 'vue-good-table/dist/vue-good-table.css';

Vue.use(VueGoodTable);


Vue.config.productionTip = false;

import App from './App.vue';
import router from './router';

new Vue({
  router,
  render: (h) => h(App),
}).$mount('#app');
