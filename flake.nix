{
  inputs = {
    typelevel-nix.url = "github:typelevel/typelevel-nix";
    typelevel-nix.inputs.nixpkgs.follows = "nixpkgs";
    flake-utils.follows = "typelevel-nix/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils, typelevel-nix }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          overlays = [ typelevel-nix.overlays.default ];
        };
      in {
        devShell = pkgs.devshell.mkShell ({ extraModulesPath, ... }: {
          imports = [
            typelevel-nix.typelevelShell
            "${extraModulesPath}/services/postgres.nix"
          ];
          name = "be-quiet";
          devshell.packages = [ pkgs.jujutsu ];
          typelevelShell = {
            jdk.package = pkgs.jdk23;
            native.enable = false;
            nodejs.enable = false;
          };
          services.postgres = {
            setupPostgresOnStartup = true;
          };
        });
      });
}
