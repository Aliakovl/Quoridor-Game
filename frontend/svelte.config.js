import preprocess from 'svelte-preprocess';
import typescript from '@rollup/plugin-typescript';
import adapter from '@sveltejs/adapter-node';

export default {
    preprocess: preprocess(),
    prerender: {
        default: false
    },
    kit: {
        adapter: adapter()
    },
    plugins: [
        typescript()
    ]
};
