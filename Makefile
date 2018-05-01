NAME=elastic-search

elastic:
	docker run -d \
		--rm \
		--name $(NAME) \
		-p 9200:9200 \
		-p 9300:9300 \
		-e "discovery.type=single-node" \
		docker.elastic.co/elasticsearch/elasticsearch:6.2.4

stop:
	docker stop $(NAME)
