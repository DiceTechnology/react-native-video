require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name           = 'react-native-video'
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = 'https://github.com/brentvatne/react-native-video'
  s.source       = { :git => "https://github.com/brentvatne/react-native-video.git", :tag => "#{s.version}" }
  s.swift_version = "5.0"

<<<<<<< HEAD
  s.ios.deployment_target = "12.0"
  s.tvos.deployment_target = "12.0"

  s.static_framework = true

  s.source_files = "ios/Video/*", "ios/JSProps/*", "ios/Helpers/*", "ios/DorisTypesMappers/*"
=======
  s.ios.deployment_target = "11.2"
  s.tvos.deployment_target = "11.2"

  s.static_framework = true

  s.source_files = "ios/Beacon/HTTP/*.{h,m}", "ios/Video/*.{h,m}", "ios/Beacon/*.{h,m}", "ios/Video/*.swift"
>>>>>>> ca1894edc1a8198d2025c45ac40d7f9a3458dbaf
  
  s.dependency 'React'
  s.dependency 'AVDoris'
end
