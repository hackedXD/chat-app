import { cva } from "class-variance-authority";
import { JSX } from "solid-js";
import { twMerge } from "tailwind-merge";

const labelVariants = cva("text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70");

function Label({ class: className, children, ...props }: JSX.LabelHTMLAttributes<HTMLLabelElement>) {
	return (
		<label class={twMerge(labelVariants(), className)} {...props}>
			{children}
		</label>
	);
}

export { Label, labelVariants };
