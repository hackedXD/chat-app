import { Button, buttonVariants } from "../components/Button";

import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "../components/Card";
import { Label } from "../components/Label";
import { Input } from "../components/Input";
import { twMerge } from "tailwind-merge";
import { useNavigate } from "@solidjs/router";
import { createSignal } from "solid-js";
import sha256 from "crypto-js/sha256";
import { enc } from "crypto-js";

function Login() {
	const [error, setError] = createSignal("");
	const navigate = useNavigate();

	async function onSubmit(e: Event) {
		e.preventDefault();
		if (email.value.length == 0 || password.value.length == 0) {
			setError("Invalid Input");
			return;
		}

		let data = JSON.stringify({
			username: email.value,
			password: sha256(password.value).toString(enc.Hex),
		});

		fetch("/login", {
			method: "POST",
			body: data,
			credentials: "include",
		})
			.then((res) => res.json())
			.then((res) => {
				if (res.success) {
					navigate("/chat");
				} else {
					setError(res.error);
				}
			});
	}

	let email: HTMLInputElement;
	let password: HTMLInputElement;

	return (
		<div class="dark bg-background text-primary w-screen h-screen flex flex-row">
			<div class="flex-1 flex mix-blend-difference flex-col justify-between overflow-hidden">
				<img class="absolute w-1/2 h-screen bg-repeat z-[-1] opacity-10 object-scale-down border-none bg-[url(/images/white-math-scribbles.jpg)]" />
				<Button href="/" class="w-min mt-2 ml-2" variant="link">
					<svg
						class="mr-2"
						width="15"
						height="15"
						viewBox="0 0 15 15"
						fill="none"
						xmlns="http://www.w3.org/2000/svg"
					>
						<path
							d="M6.85355 3.14645C7.04882 3.34171 7.04882 3.65829 6.85355 3.85355L3.70711 7H12.5C12.7761 7 13 7.22386 13 7.5C13 7.77614 12.7761 8 12.5 8H3.70711L6.85355 11.1464C7.04882 11.3417 7.04882 11.6583 6.85355 11.8536C6.65829 12.0488 6.34171 12.0488 6.14645 11.8536L2.14645 7.85355C1.95118 7.65829 1.95118 7.34171 2.14645 7.14645L6.14645 3.14645C6.34171 2.95118 6.65829 2.95118 6.85355 3.14645Z"
							fill="currentColor"
							fill-rule="evenodd"
							clip-rule="evenodd"
						></path>
					</svg>
					Back to Main Page
				</Button>
				<div class="p-8 flex flex-col justify-start">
					<h3 class="text-2xl font-light">"Wow This Project is definitely an A+"</h3>
					<h5 class="text-md font-semibold">
						Ms. Khan (<span class="italic">Hopefully</span>)
					</h5>
				</div>
			</div>
			<form class="flex-1 flex items-center justify-center" onSubmit={onSubmit}>
				<Button href="/signup" class="absolute right-4 top-4" variant="ghost">
					Sign Up
				</Button>
				<Card class="w-2/3">
					<CardHeader class="space-y-1">
						<CardTitle class="text-2xl">Login to your account</CardTitle>
						<CardDescription>Enter your email below to login to your account</CardDescription>
					</CardHeader>
					<CardContent class="grid gap-4">
						<div class="grid gap-2">
							<Label>Email</Label>
							<Input type="email" placeholder="someone@gmail.com" ref={email!} />
						</div>
						<div class="grid gap-2">
							<Label>Password</Label>
							<Input type="password" ref={password!} />
						</div>
					</CardContent>
					<CardFooter class="flex flex-col">
						<button type="submit" class={twMerge(buttonVariants(), "w-full")}>
							Log In
						</button>
						{error() != "" ? <Label class="text-red-500 my-3">{error()}</Label> : <></>}
					</CardFooter>
				</Card>
			</form>
		</div>
	);
}

export default Login;
