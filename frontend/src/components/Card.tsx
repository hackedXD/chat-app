import { JSX } from "solid-js";
import { twMerge } from "tailwind-merge";

function Card({ class: className, ...props }: JSX.HTMLAttributes<HTMLDivElement>) {
	return <div class={twMerge("rounded-lg border bg-card text-card-foreground shadow-sm", className)} {...props} />;
}

function CardHeader({ class: className, ...props }: JSX.HTMLAttributes<HTMLDivElement>) {
	return <div class={twMerge("flex flex-col space-y-1.5 p-6", className)} {...props} />;
}

function CardTitle({ class: className, ...props }: JSX.HTMLAttributes<HTMLHeadingElement>) {
	return <h3 class={twMerge("text-2xl font-semibold leading-none tracking-tight", className)} {...props} />;
}

function CardDescription({ class: className, ...props }: JSX.HTMLAttributes<HTMLParagraphElement>) {
	return <p class={twMerge("text-sm text-muted-foreground", className)} {...props} />;
}

function CardContent({ class: className, ...props }: JSX.HTMLAttributes<HTMLDivElement>) {
	return <div class={twMerge("p-6 pt-0", className)} {...props} />;
}

function CardFooter({ class: className, ...props }: JSX.HTMLAttributes<HTMLDivElement>) {
	return <div class={twMerge("flex items-center p-6 pt-0", className)} {...props} />;
}

export { Card, CardHeader, CardFooter, CardTitle, CardDescription, CardContent };
