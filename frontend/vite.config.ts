import { defineConfig } from "vite";
import solid from "vite-plugin-solid";

export default defineConfig({
	assetsInclude: ["src/assets/*"],
	plugins: [solid()],
});
