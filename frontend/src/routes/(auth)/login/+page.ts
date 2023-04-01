import { marked } from 'marked';

export const load: PageLoad = async ({ fetch }) => {
    const res = await fetch(`/description.md`);
    const post = await res.text();

    return {
        description: marked.parse(post)
    };
};