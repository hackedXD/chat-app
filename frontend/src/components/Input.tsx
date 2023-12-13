import { JSX } from "solid-js";
import { twMerge } from "tailwind-merge";

function Input({ class: className, ...props }: JSX.InputHTMLAttributes<HTMLInputElement>) {
	return (
		<input
			class={twMerge(
				"flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring",
				className
			)}
			{...props}
		/>
	);
}

export { Input };
