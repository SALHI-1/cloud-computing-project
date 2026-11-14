terraform {
  required_version = ">= 0.14"
  required_providers {
    openstack = {
      source  = "terraform-provider-openstack/openstack"
      version = "~> 1.54.0"
    }
  }
}

provider "openstack" {
  cloud = "devstack-admin"
}

resource "openstack_networking_secgroup_v2" "secgroup_terraform" {
  name        = "secgroup-terraform"
  description = "Groupe de securite Terraform autorisant SSH et HTTP"
}

resource "openstack_networking_secgroup_rule_v2" "rule_ssh" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 22
  port_range_max    = 22
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup_terraform.id
}

resource "openstack_networking_secgroup_rule_v2" "rule_http" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "tcp"
  port_range_min    = 80
  port_range_max    = 80
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup_terraform.id
}

resource "openstack_networking_secgroup_rule_v2" "rule_icmp" {
  direction         = "ingress"
  ethertype         = "IPv4"
  protocol          = "icmp"
  remote_ip_prefix  = "0.0.0.0/0"
  security_group_id = openstack_networking_secgroup_v2.secgroup_terraform.id
}

resource "openstack_compute_instance_v2" "ubuntu_instance" {
  name            = "ubuntu-terraform"
  image_name      = "ubuntu-22.04"
  flavor_name     = "m1.saas"
  key_pair        = "cirros-key"
  security_groups = [openstack_networking_secgroup_v2.secgroup_terraform.name]

  network {
    name = "iaas-net"
  }
}

resource "openstack_networking_floatingip_v2" "fip" {
  pool = "public"
}

resource "openstack_compute_floatingip_associate_v2" "fip_assoc" {
  floating_ip = openstack_networking_floatingip_v2.fip.address
  instance_id = openstack_compute_instance_v2.ubuntu_instance.id
}

output "instance_private_ip" {
  description = "Adresse IP privee de l'instance"
  value       = openstack_compute_instance_v2.ubuntu_instance.network[0].fixed_ip_v4
}

output "instance_floating_ip" {
  description = "Adresse IP flottante pour se connecter"
  value       = openstack_networking_floatingip_v2.fip.address
}
